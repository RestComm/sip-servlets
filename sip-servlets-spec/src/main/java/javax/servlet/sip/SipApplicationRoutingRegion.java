/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.servlet.sip;
/**
 * A class that represents the application routing region. It uses the predefined regions in the Enum SipApplicationRoutingRegionType and also allows for implementations to have additional or new regions if it is so required. This could be useful in non telephony domains where the concept of of a caller and callee is not applicable.
 * Since: 1.1
 */
public final class SipApplicationRoutingRegion{
	private SipApplicationRoutingRegionType sipApplicationRoutingRegionType = null;
	private String label = null;
    /**
     * The NEUTRAL region contains applications that do not service a specific subscriber.
     */
    public static final javax.servlet.sip.SipApplicationRoutingRegion NEUTRAL_REGION = new SipApplicationRoutingRegion("NEUTRAL", SipApplicationRoutingRegionType.NEUTRAL);

    /**
     * The ORIGINATING region contains applications that service the caller.
     */
    public static final javax.servlet.sip.SipApplicationRoutingRegion ORIGINATING_REGION= new SipApplicationRoutingRegion("ORIGINATING", SipApplicationRoutingRegionType.ORIGINATING);

    /**
     * The TERMINATING region contains applications that service the callee.
     */
    public static final javax.servlet.sip.SipApplicationRoutingRegion TERMINATING_REGION=new SipApplicationRoutingRegion("TERMINATING", SipApplicationRoutingRegionType.TERMINATING);

    /**
     * Deployer may define new routing region by constructing a new SipApplicationRoutingRegion object. The SipApplicationRoutingRegionType may be null in cases when a custom region is defined.
     */
    public SipApplicationRoutingRegion(java.lang.String label, javax.servlet.sip.SipApplicationRoutingRegionType type){
         this.label = label;
         this.sipApplicationRoutingRegionType = type;
    }

    /**
     * Each routing region has a String label.
     */
    public java.lang.String getLabel(){
        return label;
    }

    /**
     * Each routing region is either ORIGINATING, TERMINATING, or NEUTRAL type.
     */
    public javax.servlet.sip.SipApplicationRoutingRegionType getType(){
        return sipApplicationRoutingRegionType;
    }

    /**
     * {@inheritDoc}
     */
    public java.lang.String toString(){
        return label; 
    }

}
