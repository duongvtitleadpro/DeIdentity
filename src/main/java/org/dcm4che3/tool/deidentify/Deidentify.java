package org.dcm4che3.tool.deidentify;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.deident.DeIdentifier;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.tool.common.CLIUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Deidentify {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.deidentify.messages");

    private final DeIdentifier deidentifier;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;

    public Deidentify(DeIdentifier.Option... options) {
        deidentifier = new DeIdentifier(options);
    }

    public void setEncodingOptions(DicomEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        CLIUtils.addEncodingOptions(opts);
        opts.addOption(Option.builder()
                .desc(rb.getString("retain-date"))
                .longOpt("retain-date")
                .build());
        opts.addOption(Option.builder()
                .desc(rb.getString("retain-org"))
                .longOpt("retain-org")
                .build());
        opts.addOption(Option.builder()
                .desc(rb.getString("retain-dev"))
                .longOpt("retain-dev")
                .build());
        opts.addOption(Option.builder()
                .desc(rb.getString("retain-uid"))
                .longOpt("retain-uid")
                .build());
        opts.addOption(Option.builder("s")
                .hasArgs().argName("attr=value").valueSeparator('=')
                .desc(rb.getString("set"))
                .build());
        CommandLine cl = CLIUtils.parseComandLine(args, opts, rb, Deidentify.class);
        return cl;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Deidentify main = new Deidentify(options(cl));
            main.setEncodingOptions(CLIUtils.encodingOptionsOf(cl));
            main.setDummyValues(cl.getOptionValues("s"));
            final List<String> argList = cl.getArgList();
            int argc = argList.size();
            if (argc < 2)
                throw new ParseException(rb.getString("missing"));
            File dest = new File(argList.get(argc - 1));
            if ((argc > 2 || new File(argList.get(0)).isDirectory())
                    && !dest.isDirectory())
                throw new ParseException(
                        MessageFormat.format(rb.getString("nodestdir"), dest));
            for (String src : argList.subList(0, argc - 1))
                main.mtranscode(new File(src), dest);
        } catch (ParseException e) {
            System.err.println("deidentify: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("deidentify: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private void setDummyValues(String[] optVals) {
        if (optVals != null)
            for (int i = 1; i < optVals.length; i++, i++) {
                int tag = CLIUtils.toTag(optVals[i - 1]);
                VR vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
                deidentifier.setDummyValue(tag, vr, optVals[i]);
            }
    }

    private static DeIdentifier.Option[] options(CommandLine cl) {
        EnumSet<DeIdentifier.Option> options = EnumSet.noneOf(DeIdentifier.Option.class);
        if (cl.hasOption("retain-date"))
            options.add(DeIdentifier.Option.RetainLongitudinalTemporalInformationFullDatesOption);
        if (cl.hasOption("retain-dev"))
            options.add(DeIdentifier.Option.RetainDeviceIdentityOption);
        if (cl.hasOption("retain-org"))
            options.add(DeIdentifier.Option.RetainInstitutionIdentityOption);
        if (cl.hasOption("retain-uid"))
            options.add(DeIdentifier.Option.RetainUIDsOption);
        return options.toArray(new DeIdentifier.Option[0]);
    }

    private void mtranscode(File src, File dest) {
        if (src.isDirectory()) {
            dest.mkdir();
            for (File file : src.listFiles())
                mtranscode(file, new File(dest, file.getName()));
            return;
        }
        if (dest.isDirectory())
            dest = new File(dest, src.getName());
        try {
            transcode(src, dest);
            System.out.println(
                    MessageFormat.format(rb.getString("deidentified"),
                            src, dest));
        } catch (Exception e) {
            System.out.println(
                    MessageFormat.format(rb.getString("failed"),
                            src, e.getMessage()));
            e.printStackTrace(System.out);
        }
    }

    public void transcode(File src, File dest) throws IOException {
        Attributes fmi;
        Attributes dataset;
        try (DicomInputStream dis = new DicomInputStream(src)) {
            dis.setIncludeBulkData(IncludeBulkData.URI);
            fmi = dis.readFileMetaInformation();
            dataset = dis.readDataset(-1, -1);
        }
        deidentifier.deidentify(dataset);
        if (fmi != null)
            fmi = dataset.createFileMetaInformation(fmi.getString(Tag.TransferSyntaxUID));
        try (DicomOutputStream dos = new DicomOutputStream(dest)) {
            dos.setEncodingOptions(encOpts);
            dos.writeDataset(fmi, dataset);
        }
    }

}
