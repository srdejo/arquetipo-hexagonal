package co.com.srdejo.infrastructure.out.jpa.adapter;

import co.com.srdejo.domain.model.ObjectModel;
import co.com.srdejo.domain.spi.IObjectPersistencePort;
import co.com.srdejo.infrastructure.exception.NoDataFoundException;
import co.com.srdejo.infrastructure.out.jpa.entity.ObjectEntity;
import co.com.srdejo.infrastructure.out.jpa.mapper.IObjectEntityMapper;
import co.com.srdejo.infrastructure.out.jpa.repository.IObjectRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ObjectJpaAdapter implements IObjectPersistencePort {

    private final IObjectRepository objectRepository;
    private final IObjectEntityMapper objectEntityMapper;


    @Override
    public ObjectModel saveObject(ObjectModel objectModel) {
        ObjectEntity objectEntity = objectRepository.save(objectEntityMapper.toEntity(objectModel));
        return objectEntityMapper.toObjectModel(objectEntity);
    }

    @Override
    public List<ObjectModel> getAllObjects() {
        List<ObjectEntity> entityList = objectRepository.findAll();
        if (entityList.isEmpty()) {
            throw new NoDataFoundException();
        }
        return objectEntityMapper.toObjectModelList(entityList);
    }
}