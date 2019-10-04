package org.dcm4che3.tool.deidentify;

import org.dcm4che2.data.Tag;
import org.dcm4che3.data.*;
import org.dcm4che3.dcmr.DeIdentificationMethod;
import org.dcm4che3.deident.DeIdentifier;

import java.util.EnumSet;

public class DeIdentifierExtend extends DeIdentifier {
    private static final int[] Remove = {

    };
    private static final int[] Empty = {
            Tag.PatientAge
    };
    private static final int[] Dummy = {
            Tag.PatientSex,
            Tag.StudyDate,
            Tag.SeriesTime,
            Tag.Modality,
            Tag.StudyInstanceUID,
            Tag.SeriesInstanceUID,
            Tag.PatientName,
            Tag.PatientBirthDate
    };
    private EnumSet<Option> options ;
    private final Attributes dummyValues = new Attributes();
    private static final String YES = "YES";
    private static final String UNMODIFIED = "UNMODIFIED";
    private static final String REMOVED = "REMOVED";
    private int[] x ;
    private int[] y ;
    private int[] o ;

    public DeIdentifierExtend(Option... options) {
        this.options = EnumSet.of(DeIdentifierExtend.Option.BasicApplicationConfidentialityProfile, options);
        x = Remove;
        y = Empty;
        o = Dummy;
        initDummyValues(o);
    }
    private void initDummyValues(int[] d) {
        ElementDictionary dict = ElementDictionary.getStandardElementDictionary();
        for (int tag : d)
            initDummyValue(dict.vrOf(tag), tag);
        initDummyValue(VR.DA, org.dcm4che3.data.Tag.SeriesDate);
        initDummyValue(VR.TM, org.dcm4che3.data.Tag.SeriesTime);
    }
    private Object initDummyValue(VR vr, int tag) {
        return dummyValues.setString(tag, vr, dummyValueFor(vr));
    }
    private static String dummyValueFor(VR vr) {
        switch (vr) {
            case DA:
                return "19991111";
            case DT:
                return "19991111111111";
            case TM:
                return "111111";
            case IS:
            case DS:
                return "0";
            case CS:
                return "MMM";
        }
        return "REMOVED";
    }

    public enum Option {
        BasicApplicationConfidentialityProfile(DeIdentificationMethod.BasicApplicationConfidentialityProfile),
        //        CleanPixelDataOption(DeIdentificationMethod.CleanPixelDataOption),
//        CleanRecognizableVisualFeaturesOption(DeIdentificationMethod.CleanRecognizableVisualFeaturesOption),
//        CleanGraphicsOption(DeIdentificationMethod.CleanGraphicsOption),
//        CleanStructuredContentOption(DeIdentificationMethod.CleanStructuredContentOption),
//        CleanDescriptorsOption(DeIdentificationMethod.CleanDescriptorsOption),
        RetainLongitudinalTemporalInformationFullDatesOption(
                DeIdentificationMethod.RetainLongitudinalTemporalInformationFullDatesOption),
        //        RetainLongitudinalTemporalInformationModifiedDatesOption(
//                DeIdentificationMethod.RetainLongitudinalTemporalInformationModifiedDatesOption),
//        RetainPatientCharacteristicsOption(DeIdentificationMethod.RetainPatientCharacteristicsOption),
        RetainDeviceIdentityOption(DeIdentificationMethod.RetainDeviceIdentityOption),
        RetainInstitutionIdentityOption(DeIdentificationMethod.RetainInstitutionIdentityOption),
        RetainUIDsOption(DeIdentificationMethod.RetainUIDsOption);
//        RetainSafePrivateOption(DeIdentificationMethod.RetainSafePrivateOption);

        private final Code code;

        Option(Code code) {
            this.code = code;
        }
    }

    @Override
    public void deidentify(Attributes attrs) {
        deidentifyItem(attrs);
        correct(attrs);
        attrs.setString(org.dcm4che3.data.Tag.PatientIdentityRemoved, VR.CS, YES);
        attrs.setString(org.dcm4che3.data.Tag.LongitudinalTemporalInformationModified, VR.CS,
                options.contains(Option.RetainLongitudinalTemporalInformationFullDatesOption) ? UNMODIFIED : REMOVED);
        Sequence sq = attrs.ensureSequence(org.dcm4che3.data.Tag.DeidentificationMethodCodeSequence, options.size());
        for (Option option : options) {
            sq.add(option.code.toItem());
        }
    }

    private void deidentifyItem(Attributes attrs) {
        attrs.removePrivateAttributes();
        attrs.removeCurveData();
        attrs.removeOverlayData();
        attrs.removeSelected(x);
        attrs.replaceSelected(dummyValues, o);
      /*if (!options.contains(Option.RetainUIDsOption))
         attrs.replaceUIDSelected(u);*/

        try {
            attrs.accept(new Attributes.Visitor() {
                @Override
                public boolean visit(Attributes attrs, int tag, VR vr, Object value) throws Exception {
                    if (value instanceof Sequence)
                        for (Attributes item : (Sequence) value)
                            deidentifyItem(item);
                    return true;
                }
            }, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void correct(Attributes attrs) {
        if (!options.contains(DeIdentifier.Option.RetainLongitudinalTemporalInformationFullDatesOption)
                && UID.PositronEmissionTomographyImageStorage.equals(attrs.getString(org.dcm4che3.data.Tag.SOPClassUID))) {
            attrs.setString(org.dcm4che3.data.Tag.SeriesDate, VR.DA, dummyValues.getString(org.dcm4che3.data.Tag.SeriesDate));
            attrs.setString(org.dcm4che3.data.Tag.SeriesTime, VR.TM, dummyValues.getString(org.dcm4che3.data.Tag.SeriesTime));
        }
    }

}
