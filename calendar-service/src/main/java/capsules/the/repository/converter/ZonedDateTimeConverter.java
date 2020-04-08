package capsules.the.repository.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Converter(autoApply = true)
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, OffsetDateTime> {

    @Override
    public OffsetDateTime convertToDatabaseColumn(ZonedDateTime dateTime) {
        return dateTime.toOffsetDateTime();
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(OffsetDateTime dateTime) {
        return dateTime.atZoneSameInstant(ZoneId.of("UTC"));
    }
}
