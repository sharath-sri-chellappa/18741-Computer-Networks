%% wifireceiver: Unpacks a Wi-Fi packet to get data
% output = Unpacked WiFi packet
% lengthofmessage = Length of the data after unpacking
% paddedatbeginning = Zeros padded at beginning
% Inputs: message = text message,
% level = number of stages of encoding
function [output,lengthofmessage, paddedatbeginning] = wifireceiver_submitted(message, level)

    %% Default values
    if(nargin < 2)
        level = 5;
    end

    lengthofmessage = 0;
    paddedatbeginning = 0;
    
    %% Sanity checks
    
    % check if message length is reasonable
    if(length(message) > 10000)
        fprintf(2, 'Error: Message too long\n');
        output=[];
        return;
    end
    
   
    % check if level is between 1 and 5
    if(level > 5 || level < 1)
        fprintf(2, 'Error: Invalid level, must be 1-5\n');
        output=[];
        return;
    end
        
    %% Some constants
    
    % We will split the data into a cluster of nfft bits
    nfft = 64;
    % This is the Encoder/decoder trellis used by WiFi's turbo encoder
    Trellis = poly2trellis(7,[133 171]); 
    % Every WiFi packet will start with this exact preamble
    preamble = [1, 1, 1, 1,-1,-1, 1, 1,-1, 1,-1, 1, 1, 1, 1, 1, 1,-1,-1, 1, 1,-1, 1,-1, 1, 1, 1, 1, 1,-1,-1, 1, 1,-1, 1,-1, 1,-1,-1,-1,-1,-1, 1, 1,-1, -1, 1,-1, 1,-1, 1, 1, 1, 1,-1,-1, -1,-1,-1, 1, 1,-1, -1, 1];
    % Every 64 bits are mixed up like below:
    %Interleave = reshape(reshape([1:nfft], 4, []).', [], 1);

    %% Lets learn about the message
    % Length
    len = length(message);
    output = message;

    %% Level #5: Lets remove the random padding and noise
    if (level >= 5)
        % Here we are trying to find the first peak which will correspond to the actual data
        output = message;
        peakfindingop = abs(output);
        [pks,locs] = findpeaks(peakfindingop);
        count = 1;
        countpad = 1;
        for i = 1:length(pks)
            if (pks(i)>6)
                threshpks(count)=pks(i);
                locsfinal(count)=locs(i);
                count=count+1;
            else
                countpad = countpad + 1;
            end
        end
        paddedatbeginning = locsfinal(1)-2;
        finalop = output(locsfinal(1)-1:locsfinal(count-1));
        output = finalop;
        
        lastbitindex = length((output));
        % disp('last bit index');
        % disp(lastbitindex);
        bitnum = mod(lastbitindex,64);
        % disp(bitnum);
        if (bitnum < 64)
            remsub = 64 - bitnum;
        else
            remsub = 0;
        end
        a=zeros(1,remsub);
        output = [output,a];
    end
    %% Level #4: Next, lets remove the OFDM packet
    if (level >= 4)
       % Number of symbols in message
       nsym = length(output)/nfft;
       for ii = 1:nsym
            % Collect the iith symbol
            symbol = output((ii-1)*nfft+1:ii*nfft);
            % Run an IFFT on the symbol
            output((ii-1)*nfft+1:ii*nfft) = ifft(symbol);
       end       
       ro = round(output);
       for n = 1:length(ro)
          if ro(n) == 0
             ro(n) = -1;
          end
       end
       output = ro;
       message = output;
    end
    %% Level #3: Next, lets do BPSK demodulation, which unmaps the bits to a modulation
    if (level >= 3)
        output = message;
        % Remove the Preamble from the message
        prelen = length(preamble);
        % Do BPSK Demod
        output = output(prelen+1:end);
        output = (output+1)/2;
        message = output;
    end
     
    %% Level #2: Next, lets do interleaving, which permutes the bits
    if (level >= 2)
       % Number of symbols in message
       output = message;
%        disp('Length of output at start - Level 2');
%        disp(length(output));
       nsym = length(message)/nfft;
       output = message;
        for ii = 1:nsym
            % Collect the iith symbol
            symbol = output((ii-1)*nfft+1:ii*nfft);
            % Interleave the symbol
            interleave1 = reshape(symbol, [], 4).';
            interleave2 = reshape(interleave1,1,[]);
            output((ii-1)*nfft+1:ii*nfft) = interleave2;
        end
        output2 = round(abs(output));
        output = output2;
        message = output2;
    end
    
    %% Level #1:Removing Redundancy in the bits
    if (level >= 1)
        output = message;
        msgoutput = message(65:end);
        tb = 1;
        msgoutput1 = vitdec(msgoutput,Trellis,tb,'trunc','hard');
        lastbitindex = find(msgoutput1,1,'last');
        bitnum = mod(lastbitindex,8);
        if (bitnum < 8)
            remsub = 8 - bitnum;
        else
            remsub = 0;
        end
        datawithoutzeros = msgoutput1(1:lastbitindex);
        output2 = [datawithoutzeros,zeros(1,remsub)];
        reshapeout1 = reshape((output2.'),8,[]);
        reshapeout2 = reshape((reshapeout1.'),[],8);
        output1 = num2str(reshapeout2);
        output3 = bin2dec(output1);
        output = char(output3);
        output = reshape(output,1,[]);
        output2 = strcat(output)
        output = output2
        lengthofmessage = length(output);
    end
    


end
