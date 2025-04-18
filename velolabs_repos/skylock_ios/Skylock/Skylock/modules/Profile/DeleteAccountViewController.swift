//
//  DeleteAccountViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 02/02/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import KeychainSwift
import FBSDKLoginKit
import Crashlytics
import RestService

class DeleteAccountViewController: SLBaseViewController {
    
    static var storyboard: DeleteAccountViewController {
        let sb = UIStoryboard(name: "Profile", bundle: nil)
        return sb.instantiateViewController(withIdentifier: "deleteAccount") as! DeleteAccountViewController
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        title = "DELETE ACCOUNT".localized()
        addBackButton()
    }
    
    @IBAction func deleteAction(_ sender: Any) {
        presentLoadingViewWithMessage(message: "Deleting account".localized())
        Oval.users.delete(success: { [weak self] in
            self?.performDelete()
        }, fail: { [weak self] error in
            let header = "Server Error.".localized()
            let info = "Sorry, we were unable to delete your account. Please try again.".localized()
            self?.dismissLoadingViewWithCompletion(completion: {
                self?.presentWarningViewControllerWithTexts(texts: [.Header: header,
                                                                    .Info: info, .CancelButton: "OK".localized()],
                                                            cancelClosure: nil)
            })
        })
    }
    
    private func performDelete() {
        guard let user = SLDatabaseManager.shared().getCurrentUser() else { return }
        let ud:UserDefaults = UserDefaults()
        ud.set(false, forKey: SLUserDefaultsSignedIn)
        ud.synchronize()
        
        let lockManager:SLLockManager = SLLockManager.sharedManager
        lockManager.disconnectFromCurrentLock(completion: { [unowned lockManager] in
            lockManager.endActiveSearch()
        })
        KeychainSwift().clear()
        if user.userType == Oval.Users.UserType.facebook.rawValue && FBSDKAccessToken.current() != nil {
            FBSDKLoginManager().logOut()
        }
        SLDatabaseManager.shared().delete(user)
        SLLockManager.sharedManager.removeAllLocks()
        
        dismiss(animated: true, completion: {
            let svc = SLSignInViewController()
            let appDelegate = UIApplication.shared.delegate as! SLAppDelegate
            let navigation = appDelegate.window.rootViewController as? UINavigationController
            navigation?.setViewControllers([svc], animated: true)
        })
        
        Answers.logCustomEvent(withName: "Delete account", customAttributes: nil)
    }
}
