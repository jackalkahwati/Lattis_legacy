//
//  NetworkServices.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/3/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreLocation

protocol MediaServer {
    func download(by url: URL, success: @escaping (Data) -> (), fail: @escaping (Error) -> ())
}

