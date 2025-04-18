//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 06.04.2022.
//

import Foundation

public protocol SASBackend {
    func token(for nonce: String, device: String) async throws -> String
}
