package com.lattis.ellipse.domain.interactor.uploadImage;

import com.lattis.ellipse.data.network.model.response.uploadImage.UploadImageResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.UploadImageRepository;

import javax.inject.Inject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import io.reactivex.Observable;

/**
 * Created by Velo Labs Android on 06-04-2017.
 */

public class UploadImageUseCase extends UseCase<UploadImageResponse> {
    UploadImageRepository uploadImageRepository;
    private String uploadType;
    private MultipartBody.Part file;
    RequestBody   fileName;

    @Inject
    protected UploadImageUseCase(ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread,UploadImageRepository uploadImageRepository) {
        super(threadExecutor, postExecutionThread);
        this.uploadImageRepository = uploadImageRepository;
    }

    public UploadImageUseCase withUploadType(String uploadType) {
        this.uploadType = uploadType;
        return this;
    }

    public UploadImageUseCase withFile( MultipartBody.Part file) {
        this.file = file;
        return this;
    }

    @Override
    protected Observable<UploadImageResponse> buildUseCaseObservable() {
        return uploadImageRepository.uploadImage(uploadType, file);
    }


}
