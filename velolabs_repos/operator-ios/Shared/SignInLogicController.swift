//
//  SignInLogicController.swift
//  Operator
//
//  Created by Ravil Khusainov on 02.03.2021.
//

import Foundation
import Combine
import SwiftUI

final class SignInLogicController: ObservableObject {
    
    fileprivate let settings: UserSettings
    @Published var state: ViewState = .screen
    @Published var email: String = ""
    @Published var password: String = ""
    
    fileprivate var cancellables: Set<AnyCancellable> = []
    
    init(_ settings: UserSettings) {
        self.settings = settings
    }
    
    func validate() -> Bool {
        email.isValidEmail && password.count > 5
    }
    
    func login() {
        state = .loading
        CircleAPI.login(user: .init(email: email, password: password))
            .sink(receiveCompletion: { [unowned self] (result) in
                switch result {
                case .failure(let error):
                    self.state = .error(nil, error.localizedDescription)
                case .finished:
                    break
                }
            }, receiveValue: { [unowned self] auth in
                guard !auth.token.isEmpty else { return }
                self.settings.loggedIn(auth: auth)
            })
            .store(in: &cancellables)
    }
    
    func login(_ user: FleetOperator.Demo) {
        state = .loading
        CircleAPI.logIn(user.token)
        CircleAPI.user()
            .sink { [unowned self] (result) in
                switch result {
                case .failure(let error):
                    self.state = .error(nil, error.localizedDescription)
                case .finished:
                    break
                }
            } receiveValue: { [unowned self]  oper in
                self.settings.loggedIn(auth: .init(token: user.token, operator: oper))
            }
            .store(in: &cancellables)
    }
}

extension FleetOperator {
    struct Demo: Identifiable {
        let name: String
        let token: String
        
        var id: String { name }
        
        #if DEV
        static let ravil = Demo(name: "Ravil", token: "7cc84e2eed2862859e06c1cdebfe46322f9bd161333e13b9fef40389469e2df0993887bb215855ca16fc736c8cea25d8")
        static let jeremy = Demo(name: "Jeremy", token: "3adfad62189531286fa93768455924ffc87f3f73b753d589cd2821e4647d0d0674bde560937596fcbe6c6352e6343122")
        static let jack = Demo(name: "Jack", token: "a3e340e6b6f0b29b9e13fd60be19be0e619f5e3c76e99073e0b47ecb74916b9f3108f417f349f1f8ec39808d580212b3")
        static let marcus = Demo(name: "Marcus", token: "66cc47318c545a2c9bab59f8783dd57310554a238ae7bd4a2762213435ae8e16b6ba4cf5d89f80eed8b9412e7248c01f")
        #endif
        #if PROD
        static let jack = Demo(name: "Jack", token: "a3e340e6b6f0b29b9e13fd60be19be0e619f5e3c76e99073e0b47ecb74916b9f3108f417f349f1f8ec39808d580212b3")
        static let ravil = Demo(name: "Ravil", token: "7cc84e2eed2862859e06c1cdebfe4632846ecaf219ada0ab5cfaf473a74e72f7dfee4a84eabe0f4442c8168e52f335bb")
        static let jeremy = Demo(name: "Jeremy", token: "f63d73d6749734f58222a6e931ea5b78c87f3f73b753d589cd2821e4647d0d06e28d84bb2470d7e6052039a127981b98")
        static let trevor = Demo(name: "Trevor", token: "345262b7d06cd3f6df2bc6307192ca43576597ff6f99b54540a5df5f0ad010f3944ebb16eb532960bb8d50f84dc370b3")
        static let stephen = Demo(name: "Stephen", token: "e7d15595547f38f7babc9f53fdb4202ccab7cd62d39688a644667aaeab2a92f5258ccfb7cf18396be3d16e48bad209a622b0dd6a6d9da7ab7b07773453d33ddc")
        static let robert = Demo(name: "Robert O", token: "70eb2e7ccc01bee9facab776b00c39d8181934a33db511e4786d2d0bb045dac64caff00fa048334987891447481a4b7e")
        static let madi = Demo(name: "Madi", token: "6ae695332d5e5446d9d05cccd0cf353ebeca5df076e6cc82cc89efd923a9f49cefd2f8ed052634f265968c25f0231c2e")
        static let amiad = Demo(name: "Amiad", token: "e6849b880708b62d67211e3f1ded4ab9c9a97857af911cb048c0e8706f60b90d304a4129e631ab76f5aafc398a45aa8ff3f7d463806b1d87c861f840d2b4b888")
        static let goscoot = Demo(name: "GoScoot", token: "5fd14a728e5c107ec688f44797d5762b15b4b3ed8545de3245da33a7273118e534e5d6a3cc9f691a851fb7dc1c3438be")
        static let marcus = Demo(name: "Marcus", token: "66cc47318c545a2c9bab59f8783dd57310554a238ae7bd4a2762213435ae8e16ea25f61741f8b59d2c22ec749b610c5b")
        static let unlimited = Demo(name: "Unlim", token: "e6849b880708b62d67211e3f1ded4ab9c9a97857af911cb048c0e8706f60b90d304a4129e631ab76f5aafc398a45aa8ff3f7d463806b1d87c861f840d2b4b888")
        static let scansca = Demo(name: "Scansca", token: "72c80653ad5b9b1f1faa768e3f3b1587637d0b480eeaf33c55e8a8726bfc1421460c4585ac0628aab71c3dca44b5c58ff3f7d463806b1d87c861f840d2b4b888")
        #endif
    }
}

extension Array where Element == FleetOperator.Demo {
    static var dummy: Self {
        #if DEV
        return [.ravil, .jeremy, .jack, .marcus]
        #elseif PROD
        return [.jack, .jeremy, .madi, .marcus]
        #else
        return []
        #endif
    }
}

extension String  {
    var isValidEmail: Bool {
        let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailTest.evaluate(with: self)
    }
}
