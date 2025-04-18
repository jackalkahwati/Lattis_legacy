//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 31.03.2022.
//

import Foundation

public protocol SASBackend {
    func token(for nonce: String, device: String) async throws -> String
}
