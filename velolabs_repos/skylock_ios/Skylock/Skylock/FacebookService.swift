//
//  FacebookService.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 18/12/2016.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import FBSDKLoginKit
import RestService

class FacebookService: NSObject {
    static let shared = FacebookService()
    
    private let permissions = ["public_profile", "email", "user_friends"]
    
    func applicationBecameActive() {
        print("\(#function)")
        FBSDKAppEvents.activateApp()
    }

    func application(_ application: UIApplication, finishedLauching options: [AnyHashable: Any]) -> Bool{
        print("\(#function)")
        return FBSDKApplicationDelegate.sharedInstance().application(application, didFinishLaunchingWithOptions: options)
    }
    
    func application(_ application: UIApplication, open url: URL, sourceApplication: String, annotation: Any) -> Bool {
        print("\(#function)")
        return FBSDKApplicationDelegate.sharedInstance().application(application, open: url, sourceApplication: sourceApplication, annotation: annotation)
    }
    
    func login(viewController: UIViewController, registration:@escaping () -> (), completion: @escaping (Bool) -> ()) {
        print("\(#function)")
        let login = FBSDKLoginManager()
        if FBSDKAccessToken.current() != nil {
            login.logOut()
        }
        login.logIn(withReadPermissions: permissions, from: viewController) { (result, error) in
            var success = false
            if error != nil {
                print("Error logging into facebook \(error!)")
            } else if let res = result, res.isCancelled {
                print("Canceled login to facebook")
            } else {
                success = true
            }
            if success {
                registration()
                self.getFBUserInfo(completion: completion)
            } else {
                completion(false)
            }
        }
    }
    
    func getFBUserInfo(completion: @escaping (Bool) -> ()) {
        print("\(#function)")
        if FBSDKAccessToken.current() != nil {
            let fields = "id, name, link, first_name, last_name, picture.type(large), email"
            FBSDKGraphRequest(graphPath: "me", parameters: ["fields": fields]).start(completionHandler: { (connection, result, error) in
                if error != nil {
                    print("Error logging into facebook \(error!)")
                    completion(false)
                } else if let res = result as? [AnyHashable: Any] {
                    self.userInfoRecieved(info: res, completion: completion)
                }
            })
        }
    }
    
    func userInfoRecieved(info: [AnyHashable: Any], completion: @escaping (Bool) -> ()) {
        print("\(#function)")
        print("fetched user: \(info)")
        let ud = UserDefaults.standard
        var modifiedInfo = info
        guard let pushToken = ud.object(forKey: SLUserDefaultsPushNotificationToken) as? String else { return completion(false) }
        modifiedInfo["googlePushId"] = pushToken

        func finish(message: String) {
            print(message)
            SLDatabaseManager.shared().saveLogEntry(message)
            completion(true)
        }
        
       guard let facebookUserId = info["id"] as? String else { return completion(false) }
        SLPicManager.shared().facebookPic(forFBUserId: facebookUserId, completion: { _ in })
        guard var request = Oval.Users.Request(facebook: modifiedInfo) else { return completion(false) }
        request.facebookToken = FBSDKAccessToken.current().tokenString
        
        Oval.users.registration(user: request, success: { (userId, _) in
            Oval.users.getTokens(userId: userId, password: facebookUserId, success: { 
                completion(true)
            }, fail: { error in
                print(error)
                completion(false)
            })
            }, fail: { error in
               print(error)
               completion(false)
        })
    }
    
    func getFacebookPic(userId: String, completion: @escaping (UIImage?) -> ()) {
        print("\(#function)")
        RestService.oval.load(media: UIImage.facebook(userId: userId), success: { (image) in
            completion(image)
        }) { (error) in
            print(error)
            completion(nil)
        }
    }

}
