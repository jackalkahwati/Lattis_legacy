//
//  FacebookHelper.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/13/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import FacebookLogin
import FacebookCore

final class FacebookHelper {
    var errorhandler: (Swift.Error) -> () = {_ in}
    func login(_ viewController: UIViewController?, success: @escaping (User) -> ()) {
        if let token = AccessToken.current {
            login(token: token, success: success)
            return
        }
        let manager = LoginManager()
        manager.logIn(permissions: [.publicProfile, .email], viewController: viewController) { [weak self] (result) in
            switch result {
            case let .success(_, _, token):
                self?.login(token: token, success: success)
            case .failed(let error):
                self?.errorhandler(error)
            case .cancelled:
                self?.errorhandler(Error.canceled)
            }
        }
    }
    
    func login(token: AccessToken, success: @escaping (User) -> ()) {
        let fields = "id, first_name, last_name, picture.type(large), email"
        GraphRequest(graphPath: "me", parameters: ["fields": fields], tokenString: token.tokenString, version: nil, httpMethod: .get).start { (_, result, error) in
            if let e = error {
                self.errorhandler(e)
                return
            }
            if let res = result as? [String: Any] {
                if var user = User(res) {
                    user.token = token.tokenString
                    success(user)
                } else {
                    self.errorhandler(Error.invalidUser)
                }
                return
            }
        }
    }
    
    struct User {
        let email: String
        let id: String
        let password: String
        let countryCode: String = Locale.current.regionCode?.lowercased() ?? "us"
        var firstName: String?
        var lastName: String?
        var picture: String? = nil
        var token: String = ""
        
        init?(_ response: [String: Any]) {
            let params = response
            guard let id = params["id"] as? String,
                let email = params["email"] as? String else { return nil }
            self.id = id
            self.password = id
            self.email = email
            self.firstName = params["first_name"] as? String
            self.lastName = params["last_name"] as? String
            if let pic = params["picture"] as? [String: Any], let data = pic["data"] as? [String: Any] {
                self.picture = data["url"] as? String
            }
            UserDefaults.standard.set(self.picture, forKey: id)
            UserDefaults.standard.synchronize()
        }
        
        static func pictureUrl(for usersId: String?) -> String? {
            guard let id = usersId else { return nil }
            return UserDefaults.standard.value(forKey: id) as? String
        }
    }
    
    enum Error: Swift.Error {
        case canceled
        case invalidUser
    }
}
