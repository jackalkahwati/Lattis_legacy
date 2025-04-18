//
//  EllipseError.swift
//  EllipseLock
//
//  Created by Ravil Khusainov on 28.02.2020.
//

import Foundation

public enum EllipseError: Error {
    case missingService(EllipseBLE.Service)
    case missingChar(EllipseBLE.Characteristic)
}
