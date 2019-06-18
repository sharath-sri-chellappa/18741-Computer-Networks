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
%     if(nargin < 3)
%        snr = Inf;
%     end 

    lengthofmessage = 0;
    paddedatbeginning = 0;
    
    %% Sanity checks
    
    % check if message length is reasonable
    if(length(message) > 10000)
        fprintf(2, 'Error: Message too long\n');
        output=[];
        return;
    end
    
%     if (snr < -30)
%         fprintf(2, 'Error: SNR is lesser than 30db\n');
%         output=[];
%         return;
%     end
    
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

    %% Level #5: Finally, lets add some random padding and noise
    if (level >= 5)
        % Lets add some (random) empty space to the beginning and end
        % noise_pad_begin = zeros(1, round(rand*1000));
        % noise_pad_end = zeros(1, round(rand*1000));
        % length(noise_pad_begin)
        % arr = find(output,1,'first'):
        output = message;
        peakfindingop = abs(output);
%         disp('Length of Output at Entrance - Level 5');
%         disp(length(peakfindingop));
%         r = snr(peakfindingop);
%         disp('Snr is');
%         disp(r);
        %plot(peakfindingop)
        %if(snr>=0)

        [pks,locs] = findpeaks(peakfindingop);
        %disp(length(pks));
%         locmaxpeak = locs(1);
%         maxpeakcheck = abs(pks(2)-pks(1));
%         disp(length(peakfindingop));
%         for i = 1:(length(pks)-1)
%             peakcheck = abs(pks(i+1)-pks(i));
%             if (abs(peakcheck)>abs(maxpeakcheck))
%                 maxpeakcheck = peakcheck;
%                 locmaxpeak = locs(i);
%             end
%         end
%         disp('Value of peak difference')
%         disp(maxpeakcheck)
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
%         disp('Location of 1st peak')
%         disp(locsfinal(1)-1);
%         disp('Location of last peak')
%         disp(locsfinal(count-1));
%         disp('padded at the start')
%         disp(countpad);
        paddedatbeginning = locsfinal(1)-2;
        %a = locsfinal(count-1);
        %disp(a);
        finalop = output(locsfinal(1)-1:locsfinal(count-1));
        output = finalop;
        %end
        %secondpeak = findchangepts(output2);
%         [pks,locs] = findpeaks(output2);
%         locmaxpeak = locs(1);
%         maxpeakcheck = abs(pks(2)-pks(1));
%         disp(length(output2));
%         for i = 1:(length(pks)-1)
%             peakcheck = abs(pks(i+1)-pks(i));
%             if (abs(peakcheck)>abs(maxpeakcheck))
%                 maxpeakcheck = peakcheck;
%                 secondpeak = locs(i);
%             end
%         end
%         output = output2(1:secondpeak);
%         disp(output2);

%         disp('Second Peak Buddy')
%         disp(secondpeak);
        % disp('After Peak Finding');
        %disp(output2);
        %plot(abs(output2));
%         output = output(find(output,1,'first'):find(output,1,'last'));
%         disp('After removing 0s');
%         disp(output);
%         message = output;
        %output = [noise_pad_begin, output, noise_pad_end];
        
        % Let's add additive white gaussian noise
        % output = awgn(output, snr);
        
        
        
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
        % disp(remsub);
        a=zeros(1,remsub);
        output = [output,a];
        % disp(length(output2));
%        disp('Length of Output at End - Level 5');
%        disp(length(output));
    end
    %% Level #4: Next, lets remove the OFDM packet
    if (level >= 4)
       % Number of symbols in message
       %output = message;
       nsym = length(output)/nfft;
       for ii = 1:nsym
            % Collect the iith symbol
            symbol = output((ii-1)*nfft+1:ii*nfft);
            % Run an IFFT on the symbol
            output((ii-1)*nfft+1:ii*nfft) = ifft(symbol);
            %disp(output((ii-1)*nfft+1:ii*nfft));
%             if (output((ii-1)*nfft+1:ii*nfft)>0)
%              output((ii-1)*nfft+1:ii*nfft) = 1;
%             else
%              output((ii-1)*nfft+1:ii*nfft) = 0;
%             end
            %disp('ifft level4  i');
            %disp(output);
       end       
       ro = round(output);
       % disp('Rounded Value is');
       % disp(ro)
       % output = dec2bin(output);
       % disp('Level 4');
       for n = 1:length(ro)
          if ro(n) == 0
             ro(n) = -1;
%           else
%               ro(n) = 1;
          end
       end
       % disp(ro);
       output = ro;
       message = output;
%        disp('Length of output at end - Level 4');
%        disp(length(output));
    end
    %% Level #3: Next, lets do modulation, which maps the bits to a modulation (BPSK)
    if (level >= 3)
        % Do BPSK modulation
        %output = 2*output-1;
        % Prepend a preamble
        %output = [preamble, output];
        output = message;
%         disp('Length of output at start - Level 3');
%         disp(length(output));
        % disp("Message before demod");
        %disp(output)
        prelen = length(preamble);
        % zeroarr = zeros((1:prelen),1);
        % onearr = ones((1:length(output)-prelen),1);
        % andarr = [zeroarr,onearr];
        % output = bitand(andarr,output);
        output = output(prelen+1:end);
        % disp("Message after demod")
        %disp(output)
        output = (output+1)/2;
        % disp("Final Message")
        %disp(output)
        message = output;
%        disp('Length of output at end - Level 3');
%        disp(length(output));
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
        %disp(output2);
        output = output2;
        message = output2;
%        disp('Length of output at end - Level 2');
%        disp(length(output));
    end
    
    %% Level #1: First lets do coding, which adds redundancy to the bits
    if (level >= 1)
        % disp('hi');
        output = message;
%        disp('Length of output at start - Level 1');
%        disp(length(output));
        msgoutput = message(65:end);
        % disp(msgoutput);
        %code = convenc(ones(100,1),t);
        tb = 1;
        msgoutput1 = vitdec(msgoutput,Trellis,tb,'trunc','hard');
        % disp('Here 1 msgop')
        % disp(msgoutput1);
        lastbitindex = find(msgoutput1,1,'last');
        % disp(lastbitindex);
        bitnum = mod(lastbitindex,8);
        % disp(bitnum);
        if (bitnum < 8)
            remsub = 8 - bitnum;
        else
            remsub = 0;
        end
        % disp(remsub);
        datawithoutzeros = msgoutput1(1:lastbitindex);
        % disp('here 2 datawithoutzeros')
        % disp(datawithoutzeros);
        output2 = [datawithoutzeros,zeros(1,remsub)];
        %reshape(dec2bin(double(message).', 8).', 1, [])
        reshapeout1 = reshape((output2.'),8,[]);
        reshapeout2 = reshape((reshapeout1.'),[],8);
        output1 = num2str(reshapeout2);
        output3 = bin2dec(output1);
        output = char(output3);
        %disp(output)
        %output = convertCharsToStrings(output);
        output = reshape(output,1,[]);
        output2 = strcat(output)
        output = output2
        %for i = 1:len(output)
        %    output2[i].cat(
        %message = output
        %output = mat2str(output);
        %message.append(output);
        %output = convertCharsToStrings(output);
        %disp(class(output));
        lengthofmessage = length(output);
%        disp('Length of output at end - Level 1');
%        disp(length(output));
       
        %output = output(1:end-1);
        %decoded = decoded(1:len(decoded)-len(mod(-length(bits), nfft)));
        %output = reshape[bin2dec(decoded).',8).', 1, []);
        % This basically converts the message into a sequence of bits
        %bits = reshape(dec2bin(double(message).', 8).', 1, [])-'0';
                
        % We append as many bits as necessary to make this a multiple of
        % nfft
        %bits = [bits, zeros(1, mod(-length(bits), nfft))];
        

        % Next, we apply the turbo coder
        %output = convenc(bits, Trellis);
        

       
        % Finally, let's pre-pend the length to the message
        %output = [dec2bin(len, nfft)-'0', output];

    end
    


end
