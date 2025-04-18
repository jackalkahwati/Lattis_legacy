//
//  MapObserver.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 2022-05-23.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Combine

public final class MapObserver {
    
    public static let shared = MapObserver()
    
    public var selected: PassthroughSubject<MapPoint, Never> = .init()
}
