package net.java.sip.communicator.impl.media;

import java.io.*;
import java.util.*;
import javax.media.*;
import javax.media.control.*;
import javax.media.format.AudioFormat;
import javax.media.protocol.*;

import net.java.sip.communicator.impl.media.codec.audio.Utils;

import org.eclipse.swt.graphics.Point;
import org.mobicents.sip.phone.views.SipPhoneView;
import org.mobicents.sip.phone.views.VisualizationService;


public class MonitoredPushBufferDataSource
    extends PushBufferDataSource
    implements CaptureDevice
{
    private final PushBufferDataSource dataSource;
    
    private String visualizationFilter;
    
    private int samplingRate = 44100;
    private int bps = 16;
    private boolean littleEndian = true;
    
    private long phase = Long.MIN_VALUE;

    public MonitoredPushBufferDataSource(PushBufferDataSource dataSource, String visualizationFilter)
    {
        this.dataSource = dataSource;
        this.visualizationFilter = visualizationFilter;
        PushBufferStream[] streams = dataSource.getStreams();
        for(PushBufferStream stream:streams) {
        	if(stream.getFormat() instanceof AudioFormat) {
        		AudioFormat af = (AudioFormat) stream.getFormat();
        		this.samplingRate = (int)af.getSampleRate();
        		this.bps = af.getSampleSizeInBits();
        		this.littleEndian = af.getEndian() == af.LITTLE_ENDIAN;
        	}
        }
    }

    public void connect() throws IOException
    {
        dataSource.connect();
    }

    public void disconnect()
    {
        dataSource.disconnect();
    }

    public CaptureDeviceInfo getCaptureDeviceInfo()
    {
        CaptureDeviceInfo captureDeviceInfo;

        if (dataSource instanceof CaptureDevice)
            captureDeviceInfo =
                ((CaptureDevice) dataSource).getCaptureDeviceInfo();
        else
            captureDeviceInfo = null;
        return captureDeviceInfo;
    }

    public String getContentType()
    {
        return dataSource.getContentType();
    }

    public Object getControl(String controlType)
    {
        return dataSource.getControl(controlType);
    }

    public Object[] getControls()
    {
        return dataSource.getControls();
    }

    public Time getDuration()
    {
        return dataSource.getDuration();
    }

    public FormatControl[] getFormatControls()
    {
        FormatControl[] formatControls;

        if (dataSource instanceof CaptureDevice)
            formatControls = ((CaptureDevice) dataSource).getFormatControls();
        else
            formatControls = new FormatControl[0];
        return formatControls;
    }

    public PushBufferStream[] getStreams()
    {
        PushBufferStream[] streams = dataSource.getStreams();

        if (streams != null)
            for (int streamIndex = 0; streamIndex < streams.length; streamIndex++)
                streams[streamIndex] =
                    new MonitoredPushBufferStream(streams[streamIndex]);
        return streams;
    }

    public void start() throws IOException
    {
        dataSource.start();
    }

    public void stop() throws IOException
    {
        dataSource.stop();
    }

    private class MonitoredPushBufferStream
        implements PushBufferStream
    {
        private final PushBufferStream stream;

        public MonitoredPushBufferStream(PushBufferStream stream)
        {
            this.stream = stream;
        }

        public ContentDescriptor getContentDescriptor()
        {
            return stream.getContentDescriptor();
        }

        public long getContentLength()
        {
            return stream.getContentLength();
        }

        public Object getControl(String controlType)
        {
            return stream.getControl(controlType);
        }

        public Object[] getControls()
        {
            return stream.getControls();
        }

        public Format getFormat()
        {
            return stream.getFormat();
        }

        public boolean endOfStream()
        {
            return stream.endOfStream();
        }

        public void read(Buffer buffer) throws IOException
        {
        	stream.read(buffer);

        	Object data = buffer.getData();

        	if (data != null)
        	{
        		Class<?> dataClass = data.getClass();
        		final int fromIndex = buffer.getOffset();
        		final int toIndex = fromIndex + buffer.getLength();
        		int maxValue = Integer.MIN_VALUE;
                int minValue = Integer.MAX_VALUE;
                if (Format.byteArray.equals(dataClass)) {
                	byte[] byteSignal = (byte[]) data;
                	if(bps==16) {
                		int length = toIndex - fromIndex;
                		if(length <= 0) return;
                		short[] shortSignal = Utils.byteToShortArray(byteSignal, fromIndex, toIndex - fromIndex, littleEndian);
                		for(int q=fromIndex; q<toIndex; q++) {
                    		maxValue = Math.max(shortSignal[q/2],maxValue);
                        	minValue = Math.min(shortSignal[q/2],minValue);
                        	updateValues(minValue, maxValue);
                        	phase++;
                    	}
                	} else {
	                	for(int q=fromIndex; q<toIndex; q++) {
	                		maxValue = Math.max(byteSignal[q],maxValue);
	                    	minValue = Math.min(byteSignal[q],minValue);
	                    	updateValues(minValue, maxValue);
	                    	phase++;
	                	}
                	}
                	
                } /*
                else if (Format.intArray.equals(dataClass)) {
                	int[] signal = (int[]) data;
                	for(int q=fromIndex; q<toIndex; q++) {
                		maxValue = Math.max(signal[q],maxValue);
                    	minValue = Math.min(signal[q],minValue);
                    	updateValues(minValue, maxValue);
                    	phase++;
                	}
                }
                else if (Format.shortArray.equals(dataClass)) {
                	short[] signal = (short[]) data;
                	for(int q=fromIndex; q<toIndex; q++) {
                		maxValue = Math.max(signal[q],maxValue);
                    	minValue = Math.min(signal[q],minValue);
                    	updateValues(minValue, maxValue);
                    	phase++;
                	}
                }*/

        		buffer.setData(data);
        	}

        }
        
        public void updateValues(int minValue, int maxValue) {
            int mod = (int)Math.abs(phase)%(samplingRate/8);
            if(mod == 0) {
            	VisualizationService vis = SipPhoneView.getVisualizationService(visualizationFilter);
            	if(vis != null) {
            		vis.addValue(new Point(minValue, maxValue));
            	}
            }
        }

        public void setTransferHandler(BufferTransferHandler transferHandler)
        {
            stream.setTransferHandler((transferHandler == null) ? null
                : new MonitoredBufferTransferHandler(transferHandler));
        }

        public class MonitoredBufferTransferHandler
            implements BufferTransferHandler
        {

            private final BufferTransferHandler transferHandler;

            public MonitoredBufferTransferHandler(
                BufferTransferHandler transferHandler)
            {
                this.transferHandler = transferHandler;
            }

            public void transferData(PushBufferStream stream)
            {
                transferHandler.transferData(MonitoredPushBufferStream.this);
            }
        }
    }
}
