package hmi.environment.bodyandfaceembodiments.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import hmi.environment.bodyandfaceembodiments.BodyAndFaceEmbodiment;
import hmi.environmentbase.CopyEnvironment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.faceanimation.FaceController;
import hmi.testutil.animation.HanimBody;
import hmi.xml.XMLTokenizer;

import org.junit.Test;

/**
 * Unit tests for the BodyAndFaceSpreadEmbodimentLoader
 * @author hvanwelbergen
 * 
 */
public class BodyAndFaceSpreadEmbodimentLoaderTest
{
    private BodyAndFaceEmbodiment mockBodyAndFaceEmbodiment1 = mock(BodyAndFaceEmbodiment.class);
    private BodyAndFaceEmbodiment mockBodyAndFaceEmbodiment2 = mock(BodyAndFaceEmbodiment.class);
    private EmbodimentLoader mockEmbodimentLoader1 = mock(EmbodimentLoader.class);
    private EmbodimentLoader mockEmbodimentLoader2 = mock(EmbodimentLoader.class);
    private FaceController mockFaceController1 = mock(FaceController.class);
    private FaceController mockFaceController2 = mock(FaceController.class);
    private CopyEnvironment mockCopyEnvironment = mock(CopyEnvironment.class);

    @Test
    public void test() throws IOException
    {
        when(mockEmbodimentLoader1.getEmbodiment()).thenReturn(mockBodyAndFaceEmbodiment1);
        when(mockEmbodimentLoader2.getEmbodiment()).thenReturn(mockBodyAndFaceEmbodiment2);
        when(mockBodyAndFaceEmbodiment1.getFaceController()).thenReturn(mockFaceController1);
        when(mockBodyAndFaceEmbodiment2.getFaceController()).thenReturn(mockFaceController2);
        when(mockBodyAndFaceEmbodiment1.getAnimationVJoint()).thenReturn(HanimBody.getLOA1HanimBody());
        when(mockBodyAndFaceEmbodiment2.getAnimationVJoint()).thenReturn(HanimBody.getLOA1HanimBody());

        String str = "<Loader id=\"input1\" loader=\"hmi.animationembodiments.loader.BodyAndFaceSpreadEmbodimentLoader\"/>";
        BodyAndFaceSpreadEmbodimentLoader loader = new BodyAndFaceSpreadEmbodimentLoader();

        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "vh1", "vh1", new Environment[] { mockCopyEnvironment }, new Loader[] { mockEmbodimentLoader1,
                mockEmbodimentLoader2 });

        assertNotNull(loader.getEmbodiment());
    }
}
