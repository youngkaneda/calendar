package capsules.the.repository.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.ZoneId;

@Converter(autoApply = true)
public class ZoneIdConverter implements AttributeConverter<ZoneId, String> {

    @Override
    public String convertToDatabaseColumn(ZoneId zoneId) {
        return zoneId.toString();
    }

    @Override
    public ZoneId convertToEntityAttribute(String value) {
        return ZoneId.of(value);
    }
}
