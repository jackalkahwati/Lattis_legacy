//
//  BikeFileStorage.swift
//  Lattis
//
//  Created by Ravil Khusainov on 5/1/18.
//  Copyright © 2018 Velo Labs. All rights reserved.
//

import Foundation

final class BikeFileStorage: BikeStorage {
    func save(_ bike: Bike) {
        do {
            let data = try encoder.encode(bike)
            try data.write(to: fileUrl)
        } catch {
            print(error)
        }
    }
    
    func deleteAll() {
        try? fileManager.removeItem(at: fileUrl)
    }
    
    func bike(by id: Int) -> Bike? {
        do {
            let data = try Data(contentsOf: fileUrl)
            let str = String(data: data, encoding: .utf8)
            print(str)
            let bike = try decoder.decode(Bike.self, from: data)
            return bike
        } catch {
            print(error)
            return nil
        }
    }
    
    let encoder = JSONEncoder()
    let decoder = JSONDecoder()
    let fileManager = FileManager.default
    
    init() {
        self.decoder.keyDecodingStrategy = .convertFromSnakeCase
        self.decoder.dateDecodingStrategy = .secondsSince1970
        self.encoder.keyEncodingStrategy = .convertToSnakeCase
        self.encoder.outputFormatting = .prettyPrinted
        self.encoder.dateEncodingStrategy = .secondsSince1970
    }
    
    fileprivate let filename = "bike.json"
    fileprivate var fileUrl: URL {
        var url = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first!
        url.appendPathComponent(filename)
        return url
    }
}
