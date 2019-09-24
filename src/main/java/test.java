import org.dcm4che3.deident.DeIdentifier;
import org.dcm4che3.tool.deidentify.Deidentify;


import java.io.File;
import java.io.IOException;

public class test {
    public static void main(String[] args) {
        File ititialFile = new File("D:\\[MCC]DICOM Test\\5a7301a0e4b0b4037cf4bfed.dcm");
        File ititialFile2 = new File("D:\\[MCC]DICOM Test\\5a7301a0e4b0b4037cf4bfee.dcm");
        Deidentify deidentify = new Deidentify(DeIdentifier.Option.RetainUIDsOption);
        try {
            deidentify.transcode(ititialFile,ititialFile2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
