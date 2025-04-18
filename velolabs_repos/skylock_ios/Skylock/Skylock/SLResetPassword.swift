//
//  SLResetPassword.swift
//  Ellipse
//
//  Created by S Rupesh Kumar on 21/10/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import Foundation

class SLResetPassword : NSObject{
    
    public func resetPassword(completion: @escaping (_ status: UInt, _ response: [AnyHashable:Any]?) -> ()){
        
        let restManager = SLRestManager.sharedManager() as! SLRestManager
        
        let subRoutes:[String] = [
            phoneNumber,
            restManager.path(asString: .PasswordReset)
        ]
        
        
        restManager.getRequestWith(
            .main,
            pathKey: .users,
            subRoutes: subRoutes,
            additionalHeaders: nil,
            completion: { (status: UInt, textResponseDict:[AnyHashable:Any]?) in
                DispatchQueue.main.async {
                    
                    completion(status, textResponseDict)
                }
            }
        )
        
        
    }
    
}

