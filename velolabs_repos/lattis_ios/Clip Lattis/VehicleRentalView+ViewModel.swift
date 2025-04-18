//
//  VehicleRentalViewModel.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 25.01.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Combine
import AuthenticationServices
import SwiftUI

extension VehicleRentalView {
    
    final class ViewModel: ObservableObject {
        
        let qrCode: ScanView.QRType
        let startTrip: () -> Void
        
        @Published var rentalStatus: RentalStatus = .unauthorized
        @AppStorage("lattis-token")
        private var token: String?
        
        init(_ qrCode: ScanView.QRType, startTrip: @escaping () -> Void) {
            self.startTrip = startTrip
            self.qrCode = qrCode
            if let token = token {
                OvalAPI.logIn(token)
                rentalStatus = .authorized
            }
        }
        
        @MainActor
        func signIn(with result: Result<ASAuthorization, Error>) {
            switch result {
            case .success(let authorization):
                guard let user = OAuthUser(authorization.credential) else { return }
                Task.detached {
                    await self.authenticate(user)
                }
            case .failure(let error):
                print(error)
            }
        }
        
        nonisolated fileprivate func authenticate(_ user: OAuthUser) async {
            do {
                let usr = try await CircleAPI.signIn(user)
                self.token = usr.token
                OvalAPI.logIn(usr.token)
                self.rentalStatus = .authorized
            } catch {
                print(error)
            }
        }
    }
    
    enum RentalStatus {
        case unauthorized
        case authorized
    }
}


struct OAuthUser: Codable {
    let firstName: String?
    let lastName: String?
    let email: String?
    let user: String
    let identityToken: String
    let authorizationCode: String
    let countryCode: String
}

extension OAuthUser {
    init?(_ credential: ASAuthorizationCredential) {
        guard
            let cred = credential as? ASAuthorizationAppleIDCredential,
            let t = cred.identityToken, let token = String(data: t, encoding: .utf8),
            let c = cred.authorizationCode, let code = String(data: c, encoding: .utf8)
        else { return nil }
        self.firstName = cred.fullName?.givenName
        self.lastName = cred.fullName?.familyName
        self.email = cred.email
        self.user = cred.user
        self.identityToken = token
        self.authorizationCode = code
        self.countryCode = Locale.current.regionCode ?? "us"
    }
}
