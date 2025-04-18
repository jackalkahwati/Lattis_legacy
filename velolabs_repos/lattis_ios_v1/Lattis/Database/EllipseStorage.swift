//
//  EllipseStorage.swift
//  Lattis
//
//  Created by Ravil Khusainov on 14/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

protocol EllipseStorage {
    func ellipse(with macId: String) -> Ellipse?
    func ellipse(with lockId: Int32) -> Ellipse?
    func save(ellipse: Ellipse)
    func delete(ellipse: Ellipse)
}
