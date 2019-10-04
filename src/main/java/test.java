import org.dcm4che3.deident.DeIdentifier;
import org.dcm4che3.tool.deidentify.DeIdentifyExtend;
import org.dcm4che3.tool.deidentify.DeIdentifierExtend;
import org.dcm4che3.tool.deidentify.Deidentify;


import java.io.File;
import java.io.IOException;

public class test {
    public static void main(String[] args) {
        File ititialFile = new File("D:\\GitHub\\DeIdentity\\target\\19665.dcm");
        File ititialFile2 = new File("D:\\GitHub\\DeIdentity\\target\\123456.dcm");
        //Deidentify deidentify = new Deidentify(DeIdentifier.Option.BasicApplicationConfidentialityProfile);
        DeIdentifyExtend deIdentifyExtend = new DeIdentifyExtend(DeIdentifierExtend.Option.BasicApplicationConfidentialityProfile);

        try {
            deIdentifyExtend.transcode(ititialFile,ititialFile2);
    /*        ititialFile.delete();
            ititialFile2.renameTo(ititialFile);*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
