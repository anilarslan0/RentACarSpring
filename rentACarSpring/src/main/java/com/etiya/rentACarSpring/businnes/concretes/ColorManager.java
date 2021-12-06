package com.etiya.rentACarSpring.businnes.concretes;

import com.etiya.rentACarSpring.businnes.dtos.ColorSearchListDto;
import com.etiya.rentACarSpring.core.utilities.results.DataResult;
import com.etiya.rentACarSpring.core.utilities.results.SuccesDataResult;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.etiya.rentACarSpring.businnes.abstracts.ColorService;
import com.etiya.rentACarSpring.businnes.constants.Messages;
import com.etiya.rentACarSpring.businnes.request.ColorRequest.CreateColorRequest;
import com.etiya.rentACarSpring.businnes.request.ColorRequest.DeleteColorRequest;
import com.etiya.rentACarSpring.businnes.request.ColorRequest.UpdateColorRequest;
import com.etiya.rentACarSpring.core.utilities.mapping.ModelMapperService;

import com.etiya.rentACarSpring.core.utilities.results.Result;

import com.etiya.rentACarSpring.core.utilities.results.SuccesResult;
import com.etiya.rentACarSpring.dataAccess.abstracts.ColorDao;

import com.etiya.rentACarSpring.entities.Color;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColorManager implements ColorService {

    private ColorDao colorDao;
    private ModelMapperService modelMapperService;

    @Autowired
    public ColorManager(ColorDao colorDao, ModelMapperService modelMapperService) {
        super();
        this.colorDao = colorDao;
        this.modelMapperService = modelMapperService;
    }

    @Override
    public DataResult<List<ColorSearchListDto>> getAll() {
        List<Color> result = this.colorDao.findAll();
        List<ColorSearchListDto> response = result.stream()
                .map(color -> modelMapperService.forDto().map(color, ColorSearchListDto.class))
                .collect(Collectors.toList());

        return new SuccesDataResult<List<ColorSearchListDto>>(response);
    }

    @Override
    public Result save(CreateColorRequest createColorRequest) {
        Color color = modelMapperService.forRequest().map(createColorRequest, Color.class);
        this.colorDao.save(color);
        return new SuccesResult(Messages.addedColor);
    }

    @Override
    public Result update(UpdateColorRequest updateColorRequest) {
        Color color = modelMapperService.forRequest().map(updateColorRequest, Color.class);
        this.colorDao.save(color);
        return new SuccesResult(Messages.updatedColor);
    }

    @Override
    public Result delete(DeleteColorRequest deleteColorRequest) {
        this.colorDao.deleteById(deleteColorRequest.getColorId());
        return new SuccesResult(Messages.deletedColor);
    }

}
