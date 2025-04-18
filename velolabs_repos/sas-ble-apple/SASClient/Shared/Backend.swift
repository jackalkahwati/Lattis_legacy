//
//  Backend.swift
//  SASClient
//
//  Created by Ravil Khusainov on 31.03.2022.
//

import Foundation
import OvalBackend
import SasBle

extension OvalBackend: SASBackend {
    public func token(for nonce: String, device: String) async throws -> String {
        struct Tkn: Codable {
            let token: String
        }
        let result: Tkn = try await get(.init(.sas, path: "credentials/\(device.uppercased())/\(nonce.uppercased())", query: [.init(name: "fleetId", value: "23")]))
        return result.token
    }
}
