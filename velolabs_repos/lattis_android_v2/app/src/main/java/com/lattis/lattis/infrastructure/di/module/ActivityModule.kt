package com.lattis.lattis.infrastructure.di.module

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.models.Ride
import com.lattis.lattis.executor.UiThread
import com.lattis.lattis.infrastructure.di.scope.ViewScope
import com.lattis.lattis.presentation.authentication.forgotpassword.ForgotPasswordActivity
import com.lattis.lattis.presentation.authentication.launch.LaunchActivity
import com.lattis.lattis.presentation.authentication.signin.SignInActivity
import com.lattis.lattis.presentation.authentication.signup.SignUpActivity
import com.lattis.lattis.presentation.authentication.verifycode.EnterSecretCodeActivity
import com.lattis.lattis.presentation.damage.ReportDamageActivity
import com.lattis.lattis.presentation.fleet.PrivateFleetActivity
import com.lattis.lattis.presentation.fleet.add.EmailSecretCodeVerificationActivity
import com.lattis.lattis.presentation.help.HelpActivity
import com.lattis.lattis.presentation.history.RideHistoryActivity
import com.lattis.lattis.presentation.history.detail.RideHistoryDetailActivity
import com.lattis.lattis.presentation.home.activity.HomeActivity
import com.lattis.lattis.presentation.membership.MembershipActivity
import com.lattis.lattis.presentation.parking.ParkingActivity
import com.lattis.lattis.presentation.payment.PaymentActivity
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity
import com.lattis.lattis.presentation.payment.add.AddPromotionActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.popup.edit.PopUpEditActivity
import com.lattis.lattis.presentation.profile.ProfileActivity
import com.lattis.lattis.presentation.qrscan.ScanBikeQRCodeActivity
import com.lattis.lattis.presentation.qrscan.dummy.DummyLoadingActivity
import com.lattis.lattis.presentation.reservation.ReservationActivity
import com.lattis.lattis.presentation.reservation.ReservationListOrCreateActivity
import com.lattis.lattis.presentation.reservation.edit.ReservationEditActivity
import com.lattis.lattis.presentation.ride.EndRideActivity
import com.lattis.lattis.presentation.ride.RideSummaryActivity
import com.lattis.lattis.presentation.search_places.SearchPlacesActivity
import com.lattis.lattis.presentation.webview.WebviewActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @Binds
    abstract fun bindPostExecutionThread(uiThread: UiThread): PostExecutionThread

    @ViewScope
    @ContributesAndroidInjector
    abstract fun LaunchActivityInjector(): LaunchActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun SignInActivityInjector(): SignInActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun SignUpActivityInjector(): SignUpActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun HomeActivityActivityInjector(): HomeActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun EnterSecretCodeActivityInjector(): EnterSecretCodeActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun EndRidectivityInjector(): EndRideActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun PopUpctivityInjector(): PopUpActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun PopUpEditctivityInjector(): PopUpEditActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun RideSummaryActivityInjector(): RideSummaryActivity


    @ViewScope
    @ContributesAndroidInjector
    abstract fun ScanBikeQRCodeActivityInjector():ScanBikeQRCodeActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun DummyLoadingActivityInjector():DummyLoadingActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun ProfileActivityInjector(): ProfileActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun HelpActivityInjector():HelpActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun PaymentActivityInjector(): PaymentActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun PrivateFleetActivityInjector(): PrivateFleetActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun EmailSecretCodeVerificationActivityInjector():EmailSecretCodeVerificationActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun AddPaymentCardActivityInjector():AddPaymentCardActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun SearchPlacesActivityInjector():SearchPlacesActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun ReportDamageActivityInjector() : ReportDamageActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun RideHistoryActivityInjector() : RideHistoryActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun RideHistoryDetailActivityInjector() : RideHistoryDetailActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun ForgotPasswordActivityInjector():ForgotPasswordActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun ParkingActivityInjector():ParkingActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun WebviewActivityInjector():WebviewActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun ReservationActivityInjector():ReservationActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun ReservationEditActivityInjector():ReservationEditActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun ReservationListOrCreateActivityInjector():ReservationListOrCreateActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun MembershipActivityInjector():MembershipActivity

    @ViewScope
    @ContributesAndroidInjector
    abstract fun AddPromotionActivityInjector():AddPromotionActivity
}