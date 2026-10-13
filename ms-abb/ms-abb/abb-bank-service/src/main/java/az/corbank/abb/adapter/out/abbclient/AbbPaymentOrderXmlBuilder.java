package az.corbank.abb.adapter.out.abbclient;

import az.corbank.abb.domain.model.PaymentInstruction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Builds the "Payment Order XML Structure" ABB's spec documents in §5.1, for one payment,
 * wrapped in the required &lt;root&gt;&lt;payments&gt;&lt;payment&gt;...&lt;/payment&gt;&lt;/payments&gt;&lt;/root&gt;
 * envelope. AbbHttpGateway base64-encodes the result and sends it as "base64aDoc" (spec §4.3).
 *
 * "type" is "IN" (internal — spec §5.2: "Internal (IN)") since the only two flows this
 * project currently drives (own-account transfer, and a generic transfer to a named IBAN)
 * are both within-bank moves as far as ABB's classification goes; switch to "DS" (domestic)
 * or "FP" (international) here if a future flow needs those. rrn is a locally-generated
 * unique id, since the spec describes it as "unique ID generated on your side", distinct
 * from "externalReference" (the field on the outer POST body, not inside the XML itself).
 */
final class AbbPaymentOrderXmlBuilder {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private AbbPaymentOrderXmlBuilder() {
    }

    static String build(PaymentInstruction instruction) {
        String rrn = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String today = LocalDate.now().format(DATE_FORMAT);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xml.append("<root><payments><payment>");
        tag(xml, "type", "IN");
        tag(xml, "rrn", rrn);
        tag(xml, "date", today);
        tag(xml, "account", instruction.fromAccountNumber());
        tag(xml, "amount", instruction.amount().toPlainString());
        tag(xml, "paymentCurrency", instruction.currency());
        tag(xml, "recipientName", instruction.beneficiaryName());
        tag(xml, "recipientAccount", instruction.beneficiaryAccountNumber());
        tag(xml, "description1", instruction.description());
        tag(xml, "urgent", "N");
        xml.append("</payment></payments></root>");
        return xml.toString();
    }

    private static void tag(StringBuilder xml, String name, String value) {
        xml.append('<').append(name).append('>');
        if (value != null) {
            xml.append(escape(value));
        }
        xml.append("</").append(name).append('>');
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
