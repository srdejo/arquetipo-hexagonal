package co.com.srdejo.application.handler;

import co.com.srdejo.application.dto.request.ObjectRequestDto;
import co.com.srdejo.application.dto.response.ObjectResponseDto;

import java.util.List;

public interface IObjectHandler {

    void saveObject(ObjectRequestDto objectRequestDto);

    List<ObjectResponseDto> getAllObjects();
}