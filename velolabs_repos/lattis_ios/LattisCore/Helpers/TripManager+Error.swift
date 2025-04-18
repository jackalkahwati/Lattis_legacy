//
//  TripManager+Error.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 14.05.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import Foundation
import OvalAPI

extension TripManager {
    func handle(error: Error) {
        //MARK - Strip 3D secure payment authentication error
        struct Failure: Decodable {
            let error: Error
            struct Error: Decodable {
                let data: Message
                struct Message: Decodable {
                    let error_code: String
                    let payment_intent: Intent
                    
                    struct Intent: Decodable {
                        let client_secret: String
                    }
                }
            }
        }
        if let error = error as? ServerError,
           error.code == 402,
           let data = error.data {
            do {
                let message = try JSONDecoder().decode(Failure.self, from: data)
                authenticateStripe(with: message.error.data.payment_intent.client_secret)
            } catch {
                print(error)
                beginUpdate()
                send(.failure(error))
            }
            return
        }
        beginUpdate()
        send(.failure(error))
    }
}
