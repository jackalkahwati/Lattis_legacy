package com.lattis.ellipse.data.network.model.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.data.network.model.response.GetUserResponse;
import com.lattis.ellipse.data.network.model.response.PrivateNetworkResponse;
import com.lattis.ellipse.data.network.model.response.UserResponse;
import com.lattis.ellipse.domain.model.PrivateNetwork;
import com.lattis.ellipse.domain.model.User;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class UserMapper extends AbstractDataMapper<GetUserResponse,User> {

    @Inject
    public UserMapper() {}

    @NonNull
    @Override
    public User mapIn(@NonNull GetUserResponse getUserResponse) {
        User user = new User();
        UserResponse userResponse = getUserResponse.getUserResponse();
        user.setId(userResponse.getUserId());
        user.setUsersId(userResponse.getUsersId());
        user.setUsername(userResponse.getUsername());
        user.setVerified(true);
        user.setMaxLocks(userResponse.getMaxLocks());
        user.setTitle(userResponse.getTitle());
        user.setFirstName(userResponse.getFirstName());
        user.setLastName(userResponse.getLastName());
        user.setPhoneNumber(userResponse.getPhoneNumber());
        user.setEmail(userResponse.getEmail());
        List<PrivateNetworkResponse> privateNetworksResponse = getUserResponse.getPrivateNetworkResponse();
        List<PrivateNetwork>privateNetworks = new ArrayList<>();
        if(privateNetworksResponse!=null){
            for(PrivateNetworkResponse privateNetworkResponse: privateNetworksResponse){
                if(privateNetworkResponse!=null){
                    PrivateNetwork privateNetwork = new PrivateNetwork();
                    privateNetwork.setPrivate_fleet_user_id(privateNetworkResponse.getPrivate_fleet_user_id());
                    privateNetwork.setUserId(privateNetworkResponse.getUser_id());
                    privateNetwork.setEmail(privateNetworkResponse.getEmail());
                    privateNetwork.setFleet_id(privateNetworkResponse.getFleet_id());
                    privateNetwork.setVerified(privateNetworkResponse.getVerified());
                    privateNetwork.setFleet_name(privateNetworkResponse.getFleet_name());
                    privateNetwork.setType(privateNetworkResponse.getType());
                    privateNetwork.setLogo(privateNetworkResponse.getLogo());
                    privateNetworks.add(privateNetwork);

                }
            }
        }

        user.setPrivateNetworks(privateNetworks);

        return user;
    }

    @NonNull
    @Override
    public GetUserResponse mapOut(@NonNull User user) {
        return null;
    }
}
