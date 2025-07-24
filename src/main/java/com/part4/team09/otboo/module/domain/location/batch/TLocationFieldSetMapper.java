package com.part4.team09.otboo.module.domain.location.batch;

import com.part4.team09.otboo.module.domain.location.dto.response.TLocation;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

public class TLocationFieldSetMapper implements FieldSetMapper<TLocation> {

  @Override
  public TLocation mapFieldSet(FieldSet fieldSet) {
    return new TLocation(
      fieldSet.readString(0),   // category
      fieldSet.readString(1),   // code
      fieldSet.readString(2),   // level1
      fieldSet.readString(3),   // level2
      fieldSet.readString(4),   // level3
      fieldSet.readInt(5),      // gridX
      fieldSet.readInt(6),      // gridY
      fieldSet.readInt(7),      // lonD
      fieldSet.readInt(8),      // lonM
      fieldSet.readDouble(9),   // lonS
      fieldSet.readInt(10),     // latD
      fieldSet.readInt(11),     // latM
      fieldSet.readDouble(12),  // latS
      fieldSet.readDouble(13),  // longitude
      fieldSet.readDouble(14),  // latitude
      fieldSet.readString(15)   // locationUpdated (nullable 가능)
    );
  }
}
