package co.com.srdejo.infrastructure.out.jpa.mapper;

import co.com.srdejo.domain.model.ObjectModel;
import co.com.srdejo.infrastructure.out.jpa.entity.ObjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface IObjectEntityMapper {

    ObjectEntity toEntity(ObjectModel user);
    ObjectModel toObjectModel(ObjectEntity objectEntity);
    List<ObjectModel> toObjectModelList(List<ObjectEntity> userEntityList);
}