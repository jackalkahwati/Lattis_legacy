package com.lattis.ellipse.data;

import com.lattis.ellipse.data.network.model.response.uploadImage.UploadImageResponse;
import com.lattis.ellipse.data.network.store.UploadImageDataStore;
import com.lattis.ellipse.domain.repository.UploadImageRepository;

import javax.inject.Inject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Part;
import io.reactivex.Observable;


/**
 * Created by Velo Labs Android on 06-04-2017.
 */

public class UploadImageDataRepository implements UploadImageRepository {
    UploadImageDataStore uploadImageDataStore;

    @Inject
    public UploadImageDataRepository(UploadImageDataStore uploadImageDataStore) {
        this.uploadImageDataStore = uploadImageDataStore;
    }



    @Override
    public Observable<UploadImageResponse> uploadImage(String type, MultipartBody.Part part) {
       return uploadImageDataStore.uploadImage(type,part);
    }
}
