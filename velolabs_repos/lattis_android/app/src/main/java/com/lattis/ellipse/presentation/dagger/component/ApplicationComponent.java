package com.lattis.ellipse.presentation.dagger.component;

import com.lattis.ellipse.Lattis;
import com.lattis.ellipse.presentation.dagger.module.ApplicationConfig;
import com.lattis.ellipse.presentation.dagger.module.ApplicationModule;
import com.lattis.ellipse.presentation.dagger.module.AuthenticationModule;
import com.lattis.ellipse.presentation.dagger.module.BluetoothModule;
import com.lattis.ellipse.presentation.dagger.module.DeviceModule;
import com.lattis.ellipse.presentation.dagger.module.LocationModule;
import com.lattis.ellipse.presentation.dagger.module.NetworkModule;
import com.lattis.ellipse.presentation.dagger.module.RealmModule;
import com.lattis.ellipse.presentation.dagger.module.RepositoryModule;
import com.lattis.ellipse.presentation.dagger.module.SettingModule;
import com.lattis.ellipse.presentation.dagger.module.StripeModule;
import com.lattis.ellipse.presentation.dagger.module.UpdateTripServiceModule;
import com.lattis.ellipse.presentation.ui.authentication.intro.AuthenticationIntroActivity;
import com.lattis.ellipse.presentation.ui.authentication.launch.LaunchActivity;
import com.lattis.ellipse.presentation.ui.authentication.resetpassword.ConfirmCodeForForgotPasswordFragment;
import com.lattis.ellipse.presentation.ui.authentication.resetpassword.ResetPasswordActivity;
import com.lattis.ellipse.presentation.ui.authentication.resetpassword.ResetPasswordFragment;
import com.lattis.ellipse.presentation.ui.authentication.signin.SignInActivity;
import com.lattis.ellipse.presentation.ui.authentication.signup.SignUpActivity;
import com.lattis.ellipse.presentation.ui.authentication.validate.ValidateAccountActivity;
import com.lattis.ellipse.presentation.ui.authentication.verification.activity.VerificationActivity;
import com.lattis.ellipse.presentation.ui.authentication.verification.fragment.EnterSecretCodeActivity;
import com.lattis.ellipse.presentation.ui.bike.BikeBaseFragment;
import com.lattis.ellipse.presentation.ui.bike.BikeDirectionFragment;
import com.lattis.ellipse.presentation.ui.bike.BikeInfoActivity;
import com.lattis.ellipse.presentation.ui.bike.CancelRideActivity;
import com.lattis.ellipse.presentation.ui.bike.FleetParkingActivity;
import com.lattis.ellipse.presentation.ui.bike.SearchPlacesActivity;
import com.lattis.ellipse.presentation.ui.bike.TermsConditionForRide;
import com.lattis.ellipse.presentation.ui.bike.WhyBeginTripGreyOutActivity;
import com.lattis.ellipse.presentation.ui.bike.bikeList.BikeListFragment;
import com.lattis.ellipse.presentation.ui.bike.bikeList.FleetTermsConditionActivity;
import com.lattis.ellipse.presentation.ui.bike.bikeList.NoServiceActivity;
import com.lattis.ellipse.presentation.ui.bike.bikeList.ScanBikeQRCodeActivity;
import com.lattis.ellipse.presentation.ui.biketheft.ReportBikeTheft;
import com.lattis.ellipse.presentation.ui.damagebike.DamageBikeActivity;
import com.lattis.ellipse.presentation.ui.damagebike.DamageReportSuccessActivity;
import com.lattis.ellipse.presentation.ui.damagebike.DescriptionActivity;
import com.lattis.ellipse.presentation.ui.history.RideHistoryListingActivity;
import com.lattis.ellipse.presentation.ui.history.TripDetailsActivity;
import com.lattis.ellipse.presentation.ui.home.HomeActivity;
import com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragment;
import com.lattis.ellipse.presentation.ui.parking.FindParkingFragment;
import com.lattis.ellipse.presentation.ui.parking.ParkingDetailFragment;
import com.lattis.ellipse.presentation.ui.parking.ParkingMapDirectionFragment;
import com.lattis.ellipse.presentation.ui.payment.AddCardActivity;
import com.lattis.ellipse.presentation.ui.payment.PaymentInfoActivity;
import com.lattis.ellipse.presentation.ui.profile.ProfileActivity;
import com.lattis.ellipse.presentation.ui.profile.TermsAndConditionsActivity;
import com.lattis.ellipse.presentation.ui.profile.TermsAndConditionsFragment;
import com.lattis.ellipse.presentation.ui.profile.addcontact.AddMobileNumberActivity;
import com.lattis.ellipse.presentation.ui.profile.addcontact.ConfirmCodeForChangePhoneNumberActivity;
import com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailActivity;
import com.lattis.ellipse.presentation.ui.profile.changeMail.ConfirmCodeForChangeEmailActivity;
import com.lattis.ellipse.presentation.ui.profile.delete.DeleteAccountActivity;
import com.lattis.ellipse.presentation.ui.profile.fleet.AddPrivateFleetActivity;
import com.lattis.ellipse.presentation.ui.profile.help.HelpActivity;
import com.lattis.ellipse.presentation.ui.profile.logout.LogOutActivity;
import com.lattis.ellipse.presentation.ui.profile.logout.LogOutAfterEndingRideActivity;
import com.lattis.ellipse.presentation.ui.profile.updatePassword.UpdatePasswordActivity;
import com.lattis.ellipse.presentation.ui.ride.ActiveRideFragment;
import com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity;
import com.lattis.ellipse.presentation.ui.ride.EndRideFragment;
import com.lattis.ellipse.presentation.ui.ride.EndRideOutOfBoundActivity;
import com.lattis.ellipse.presentation.ui.ride.RideSummaryActivity;
import com.lattis.ellipse.presentation.ui.ride.fee.ParkingFeeActivity;
import com.lattis.ellipse.presentation.ui.ride.service.ActiveTripService;
import com.lattis.ellipse.presentation.ui.ridemenu.RideMenuActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity2;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity3;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        DeviceModule.class,
        ApplicationModule.class,
        AuthenticationModule.class,
        RealmModule.class,
        NetworkModule.class,
        RepositoryModule.class,
        SettingModule.class,
        LocationModule.class,
        BluetoothModule.class,
        UpdateTripServiceModule.class,
        StripeModule.class,
        ApplicationConfig.class})
public interface ApplicationComponent {

    void inject(Lattis lattis);

    void inject(HomeActivity homeActivity);

    void inject(ResetPasswordActivity resetPasswordActivity);

    void inject(SignInActivity signInActivity);

    void inject(SignUpActivity signUpActivity);

    void inject(DamageBikeActivity damageBikeActivity);

    void inject(DescriptionActivity descriptionActivity);

    void inject(ValidateAccountActivity validateAccountActivity);

    void inject(AuthenticationIntroActivity authenticationIntroActivity);

    void inject(TermsAndConditionsActivity termsAndConditionsActivity);

    void inject(TermsAndConditionsFragment termsAndConditionsFragment);

    void inject(EnterSecretCodeActivity enterSecretCodeActivity);

    void inject(VerificationActivity verificationActivity);

    void inject(ResetPasswordFragment resetPasswordFragment);

    void inject(BikeInfoActivity bikeInfoActivity);

    void inject(RideMenuActivity rideMenuActivity);

    void inject(CancelRideActivity cancelRideActivity);

    void inject(ConfirmCodeForForgotPasswordFragment confirmCodeForForgotPasswordFragment);

    void inject(FindParkingFragment findParkingFragment);

    void inject(ParkingDetailFragment parkingDetailFragment);

    void inject(ParkingMapDirectionFragment parkingMapDirectionFragment);

    void inject(HomeMapFragment homeMapFragment);

    void inject(ActiveRideFragment activeRideFragment);

    void inject(EndRideFragment endRideFragment);

    void inject(BikeBaseFragment bikeBaseFragment);

    void inject(BikeDirectionFragment bikeDirectionFragment);

    void inject(EndRideCheckListActivity endRideCheckListActivity);



    void inject(ActiveTripService activeTripService);

    void inject(ReportBikeTheft reportBikeTheft);

    void inject(EndRideOutOfBoundActivity endRideOutOfBoundActivity);

    void inject(PopUpActivity popUpActivity);

    void inject(ProfileActivity profileActivity);

    void inject(AddMobileNumberActivity addMobileNumberActivity);

    void inject(UpdatePasswordActivity updatePasswordActivity);

    void inject(ConfirmCodeForChangePhoneNumberActivity confirmCodeForChangePhoneNumberActivity);

    void inject(ChangeMailActivity changeMailActivity);

    void inject(ConfirmCodeForChangeEmailActivity confirmCodeForChangeEmailActivity);

    void inject(LogOutActivity logOutActivity);

    void inject(TermsConditionForRide termsConditionForRide);

    void inject(DeleteAccountActivity deleteAccountActivity);

    void inject(LogOutAfterEndingRideActivity logOutAfterEndingRideActivity);

    void inject(HelpActivity helpActivity);

    void inject(FleetParkingActivity fleetParkingZone);

    void inject(DamageReportSuccessActivity damageReportSuccessActivity);

    void inject(RideSummaryActivity rideSummaryActivity);

    void inject(NoServiceActivity serviceActivity);

    void inject(PaymentInfoActivity paymentInfoActivity);

    void inject(AddCardActivity addCardActivity);

    void inject(ParkingFeeActivity parkingFeeActivity);

    void inject(FleetTermsConditionActivity fleetTermsConditionActivity);

    void inject(RideHistoryListingActivity rideHistoryListingActivity);

    void inject(TripDetailsActivity tripDetailsActivity);

    void inject(BikeListFragment bikeListFragment);

    void inject(WhyBeginTripGreyOutActivity whyBeginTripGreyOutActivity);

    void inject(SearchPlacesActivity searchPlacesActivity);

    void inject(PopUpActivity2 popUpActivity2);

    void inject(ScanBikeQRCodeActivity scanBikeQRCodeActivity);

    void inject(LaunchActivity launchActivity);

    void inject(AddPrivateFleetActivity addPrivateFleetActivity);

    void inject(PopUpActivity3 popUpActivity3);

}
