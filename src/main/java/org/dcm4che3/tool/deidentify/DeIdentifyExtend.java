package org.dcm4che3.tool.deidentify;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;

import java.io.File;
import java.io.IOException;

public class DeIdentifyExtend extends Deidentify {

    private final DeIdentifierExtend deidentifierExtend;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;

    public DeIdentifyExtend(DeIdentifierExtend.Option... options) {
        deidentifierExtend = new DeIdentifierExtend(options);
    }

    @Override
    public void transcode(File src, File dest) throws IOException {
        Attributes fmi;
        Attributes dataset;
        try (DicomInputStream dis = new DicomInputStream(src)) {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            fmi = dis.readFileMetaInformation();
            dataset = dis.readDataset(-1, -1);
        }
        deidentifierExtend.deidentify(dataset);
        if (fmi != null)
            fmi = dataset.createFileMetaInformation(fmi.getString(Tag.TransferSyntaxUID));
        try (DicomOutputStream dos = new DicomOutputStream(dest)) {
            dos.setEncodingOptions(encOpts);
            dos.writeDataset(fmi, dataset);
        }
    }
}
