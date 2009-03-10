package net.java.sip.communicator.impl.media;

import java.io.IOException;

import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.Time;
import javax.media.control.FormatControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferStream;

import net.java.sip.communicator.impl.media.codec.audio.Utils;

import org.eclipse.swt.graphics.Point;
import org.mobicents.sip.phone.views.SipPhoneView;
import org.mobicents.sip.phone.views.VisualizationService;

import com.sun.media.protocol.BufferListener;
import com.sun.media.rtp.RTPSessionMgr;
import com.sun.media.rtp.RTPSourceStream;


public class MonitoredRTPPushDataSource
    extends com.sun.media.protocol.rtp.DataSource
    implements CaptureDevice
{

	private final com.sun.media.protocol.rtp.DataSource dataSource;
    
    private String visualizationFilter;
    
    private int samplingRate = 44100;
    private int bps = 16;
    private boolean littleEndian = true;
    private String contentType;
    private MediaLocator mediaLocator;
    
    private long phase = Long.MIN_VALUE;

    public MonitoredRTPPushDataSource(com.sun.media.protocol.rtp.DataSource dataSource, String visualizationFilter)
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
    	if(dataSource == null) return contentType;
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

    public void start() throws IOException
    {
        dataSource.start();
    }

    public void stop() throws IOException
    {
        dataSource.stop();
    }
    
    @Override
	public void flush() {
		dataSource.flush();
	}

	@Override
	public String getCNAME() {
		return dataSource.getCNAME();
	}

	@Override
	public RTPSessionMgr getMgr() {
		return dataSource.getMgr();
	}

	@Override
	public Player getPlayer() {
		return dataSource.getPlayer();
	}

	@Override
	public int getSSRC() {
		return dataSource.getSSRC();
	}

	@Override
	public PushBufferStream[] getStreams() {
		PushBufferStream[] streams = dataSource.getStreams();
		PushBufferStream[] retStreams = new PushBufferStream[streams.length];

        if (streams != null)
            for (int streamIndex = 0; streamIndex < streams.length; streamIndex++)
                retStreams[streamIndex] =
                    new MonitoredPushBufferStream(streams[streamIndex]);
        return retStreams;
	}

	@Override
	public boolean isPrefetchable() {
		return dataSource.isPrefetchable();
	}

	@Override
	public boolean isStarted() {
		return dataSource.isStarted();
	}

	@Override
	public void prebuffer() {
		dataSource.prebuffer();
	}

	@Override
	public void setBufferListener(BufferListener listener) {
		dataSource.setBufferListener(listener);
	}

	@Override
	public void setBufferWhenStopped(boolean flag) {
		dataSource.setBufferWhenStopped(flag);
	}

	@Override
	public void setChild(com.sun.media.protocol.rtp.DataSource source) {
		dataSource.setChild(source);
	}

	@Override
	public void setContentType(String contentType) {
	}

	@Override
	public void setControl(Object control) {
		dataSource.setControl(control);
	}

	@Override
	public void setLocator(MediaLocator mrl) {
		this.mediaLocator = mrl;
		if(dataSource != null)
		dataSource.setLocator(mrl);
	}

	@Override
	public void setMgr(RTPSessionMgr mgr) {
		dataSource.setMgr(mgr);
	}

	@Override
	public void setPlayer(Player player) {
		dataSource.setPlayer(player);
	}

	@Override
	public void setSourceStream(RTPSourceStream stream) {
		dataSource.setSourceStream(stream);
	}

	@Override
	public void setSSRC(int ssrc) {
		dataSource.setSSRC(ssrc);
	}

	@Override
	public MediaLocator getLocator() {
		if(dataSource == null) return mediaLocator;
		return dataSource.getLocator();
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
