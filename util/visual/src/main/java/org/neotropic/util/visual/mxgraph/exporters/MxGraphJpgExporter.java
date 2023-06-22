/*
 * Copyright 2021 Neotropic SAS.
 *
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

package org.neotropic.util.visual.mxgraph.exporters;

import com.neotropic.flow.component.mxgraph.MxGraph;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractImageExporter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Implements the logic to export a formated byte array of the given graph.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class MxGraphJpgExporter extends AbstractImageExporter<MxGraph> {

    @Override
    public byte [] export(MxGraph data) {
        String svg = data.getSVG();
        ByteArrayOutputStream jpg_ostream = null;
        try {
            //Workaround to get the right sizes to export
            int minWidthIndex = svg.indexOf("min-width: ");
            int minHeightIndex = svg.indexOf("min-height: ");
            int auxIndexW = svg.substring(minWidthIndex).indexOf("px");
            int auxIndexH = svg.substring(minHeightIndex).indexOf("px");
            String width = svg.substring(minWidthIndex + "min-width: ".length(), auxIndexW + minWidthIndex);
            String height = svg.substring(minHeightIndex + "min-height: ".length(), auxIndexH + minHeightIndex);

           ByteArrayInputStream is = new ByteArrayInputStream(data.getSVG().getBytes());
           DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
           DocumentBuilder db = dbf.newDocumentBuilder();

           Document doc = db.parse(is);

           NodeList imagesLst = doc.getElementsByTagName("image");
            //System.out.println(listOfStaff.getLength()); // 2

           for (int i = 0; i < imagesLst.getLength(); i++) {
                // get first staff
                Node imageNode = imagesLst.item(i);
                if (imageNode.getNodeType() == Node.ELEMENT_NODE) {
                    String uri = imageNode.getAttributes().getNamedItem("xlink:href").getTextContent();                          
                    if (uri != null && uri.length() > 1) {
                        URL sourceimage = new URL(uri);

                        Optional<StreamResource> op = 
                                VaadinSession.getCurrent().getResourceRegistry()
                                        .getResource(StreamResource.class, URI.create(sourceimage.getPath().substring(9)));
                        if (op.isPresent()) {
                              ByteArrayOutputStream oust = new ByteArrayOutputStream();
                            op.get().getWriter().accept(oust, VaadinSession.getCurrent());
                            
                            imageNode.getAttributes().getNamedItem("xlink:href").
                                    setTextContent("data:image/png;base64," + Base64.getEncoder().encodeToString(oust.toByteArray()));
                        }
                    }
                }
           }


           ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           Source xmlSource = new DOMSource(doc);
           Result outputTarget = new StreamResult(outputStream);
           TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
           InputStream iStreamResult = new ByteArrayInputStream(outputStream.toByteArray());
                       
            
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);

            TranscoderInput input_svg_image = new TranscoderInput(iStreamResult);
            jpg_ostream = new ByteArrayOutputStream();
            TranscoderOutput output_jpg_image = new TranscoderOutput(jpg_ostream);
            JPEGTranscoder my_converter = new JPEGTranscoder();
            PNGTranscoder t = new PNGTranscoder();
            my_converter.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.9));
            my_converter.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(height) + 30);
            my_converter.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(width) + 30);
            my_converter.transcode(input_svg_image, output_jpg_image);
            return jpg_ostream.toByteArray();
        } catch (TranscoderException | ParserConfigurationException | SAXException | IOException | TransformerException ex) {
            Logger.getLogger(MxGraphJpgExporter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jpg_ostream.close();
            } catch (IOException ex) {
                Logger.getLogger(MxGraphJpgExporter.class.getName()).log(Level.SEVERE, null, ex);
                return new byte[0];
            }
        }
        return new byte[0];
    }

    

}
