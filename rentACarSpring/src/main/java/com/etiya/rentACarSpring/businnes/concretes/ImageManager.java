package com.etiya.rentACarSpring.businnes.concretes;

import java.io.IOException;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.etiya.rentACarSpring.businnes.abstracts.CarService;
import com.etiya.rentACarSpring.businnes.abstracts.message.LanguageWordService;
import com.etiya.rentACarSpring.businnes.constants.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.etiya.rentACarSpring.businnes.abstracts.ImageService;
import com.etiya.rentACarSpring.businnes.constants.FilePath;
import com.etiya.rentACarSpring.businnes.dtos.ImageSearchListDto;
import com.etiya.rentACarSpring.businnes.request.ImageRequest.CreateImageRequest;
import com.etiya.rentACarSpring.businnes.request.ImageRequest.DeleteImageRequest;
import com.etiya.rentACarSpring.businnes.request.ImageRequest.UpdateImageRequest;
import com.etiya.rentACarSpring.core.utilities.businnessRules.BusinnessRules;
import com.etiya.rentACarSpring.core.utilities.helpers.FileHelper;
import com.etiya.rentACarSpring.core.utilities.mapping.ModelMapperService;
import com.etiya.rentACarSpring.core.utilities.results.DataResult;
import com.etiya.rentACarSpring.core.utilities.results.ErrorDataResult;
import com.etiya.rentACarSpring.core.utilities.results.ErrorResult;
import com.etiya.rentACarSpring.core.utilities.results.Result;
import com.etiya.rentACarSpring.core.utilities.results.SuccesDataResult;
import com.etiya.rentACarSpring.core.utilities.results.SuccesResult;
import com.etiya.rentACarSpring.dataAccess.abstracts.ImageDao;
import com.etiya.rentACarSpring.entities.Car;
import com.etiya.rentACarSpring.entities.Image;

@Service
public class ImageManager implements ImageService {

    private ImageDao imageDao;
    private FileHelper fileHelper;
    private ModelMapperService modelMapperService;
    private CarService carService;
    private LanguageWordService languageWordService;

    @Autowired
    public ImageManager(ImageDao imageDao, FileHelper fileHelper, ModelMapperService modelMapperService,
                        CarService carService, LanguageWordService languageWordService) {
        super();
        this.imageDao = imageDao;
        this.fileHelper = fileHelper;
        this.modelMapperService = modelMapperService;
        this.carService = carService;
        this.languageWordService = languageWordService;
    }

    @Override
    public Result add(CreateImageRequest createImageRequest) throws IOException {

        Result result = BusinnessRules.run(checkCarImagesCount(createImageRequest.getCarId(), 5),
                this.fileHelper.checkImageType(createImageRequest.getFile()),
                checkIfCarIsNotExistsInGallery(createImageRequest.getCarId()));

        if (result != null) {
            return result;
        }

        Date dateNow = new java.sql.Date(new java.util.Date().getTime()); // Anlık zamanı alıp bir değişkene atıyor.
        Car car = modelMapperService.forRequest().map(createImageRequest, Car.class);
        Image image = new Image();
        image.setImageUrl(
                this.fileHelper.uploadImage(createImageRequest.getCarId(), createImageRequest.getFile()).getMessage());

        image.setDate(dateNow);
        image.setCar(car);
        this.imageDao.save(image);
        return new SuccesResult(languageWordService.getByLanguageAndKeyId(Messages.CarImageUploaded));
    }

    @Override
    public Result update(UpdateImageRequest updateImageRequest) throws IOException {
        Image image = this.imageDao.getById(updateImageRequest.getImageId());

        Result result = BusinnessRules.run(checkCarImagesCount(image.getCar().getCarId(), 6),
                this.fileHelper.checkImageType(updateImageRequest.getFile()),
                checkIfImageExists(updateImageRequest.getImageId()));

        if (result != null) {
            return result;
        }

        Date dateNow = new java.sql.Date(new java.util.Date().getTime());

        image.setImageUrl(this.fileHelper.updateImage(updateImageRequest.getFile(), image.getImageUrl()).getMessage());
        image.setDate(dateNow);

        this.imageDao.save(image);

        return new SuccesResult(languageWordService.getByLanguageAndKeyId(Messages.CarImageUpdated));
    }

    @Override
    public Result delete(DeleteImageRequest deleteImageRequest) {
        Result result = BusinnessRules.run(checkIfImageExists(deleteImageRequest.getImageId())
        );
        if (result != null) {
            return result;
        }

        Image image = this.imageDao.getById(deleteImageRequest.getImageId());

        this.imageDao.delete(image);
        this.fileHelper.deleteImage(image.getImageUrl());
        return new SuccesResult(languageWordService.getByLanguageAndKeyId(Messages.CarImageDeleted));
    }

    @Override
    public DataResult<List<ImageSearchListDto>> getCarImageDetailByCarId(int carId) {
        List<Image> carImages = this.returnCarImageWithDefaultImageIfCarImageIsNull(carId).getData();

        List<ImageSearchListDto> imageSearchListDto = carImages.stream()
                .map(carImage -> modelMapperService.forDto().map(carImage, ImageSearchListDto.class))
                .collect(Collectors.toList());

        return new SuccesDataResult<List<ImageSearchListDto>>(imageSearchListDto, languageWordService.getByLanguageAndKeyId(Messages.ImageByCarIdListed));
    }

    private Result checkCarImagesCount(int CarId, int limit) {
        if (this.imageDao.countByCar_CarId(CarId) >= limit) {
            return new ErrorResult(languageWordService.getByLanguageAndKeyId(Messages.CarImageCountLimitExceed));
        }
        return new SuccesResult();
    }

    private DataResult<List<Image>> returnCarImageWithDefaultImageIfCarImageIsNull(int carId) {

        if (this.imageDao.existsByCar_CarId(carId)) {
            return new ErrorDataResult<List<Image>>(this.imageDao.getByCar_CarId(carId));
        }

        List<Image> carImages = new ArrayList<Image>();
        Image carImage = new Image();
        carImage.setImageUrl(FilePath.imagePath + FilePath.defaultImage);

        carImages.add(carImage);

        return new SuccesDataResult<List<Image>>(carImages, languageWordService.getByLanguageAndKeyId(Messages.CarImageListed));

    }


    @Override
    public DataResult<List<ImageSearchListDto>> getAll() {

        List<Image> result = this.imageDao.findAll();
        List<ImageSearchListDto> response = result.stream()
                .map(image -> modelMapperService.forDto().map(image, ImageSearchListDto.class))
                .collect(Collectors.toList());

        return new SuccesDataResult<List<ImageSearchListDto>>(response, languageWordService.getByLanguageAndKeyId(Messages.CarImageListed));

    }

    private Result checkIfImageExists(int imageId) {
        if (!this.imageDao.existsById(imageId)) {
            return new ErrorResult(languageWordService.getByLanguageAndKeyId(Messages.CarImageNotFound));
        }
        return new SuccesResult();

    }

    private Result checkIfCarIsNotExistsInGallery(int carId) {
        if (!this.carService.checkCarExistsInGallery(carId).isSuccess()) {
            return new ErrorResult(languageWordService.getByLanguageAndKeyId(Messages.CarNotFound));
        }
        return new SuccesResult();
    }

}
