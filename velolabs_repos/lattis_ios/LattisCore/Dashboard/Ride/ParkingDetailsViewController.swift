//
//  ParkingDetailsViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

enum ParkingInfo {
    case name(String)
    case details(String)
}

class ParkingDetailsViewController: UIViewController {
    
    fileprivate let tableView = UITableView()
    fileprivate let imageView = UIImageView(image: .named("parking_"))
    fileprivate let parking: Parking.Spot
    
    fileprivate var imageHeightLayout: NSLayoutConstraint!
    
    init(_ parking: Parking.Spot) {
        self.parking = parking
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        title = parking.name
        addCloseButton()
        view.backgroundColor = .white
        tableView.backgroundColor = .clear
        
        view.addSubview(imageView)
        view.addSubview(tableView)
        
        constrain(imageView, tableView, view) { image, table, view in
            image.left == view.left
            image.right == view.right
            image.top == view.safeAreaLayoutGuide.top
            self.imageHeightLayout = image.height == 200
            
            table.edges == view.edges
        }
        
        imageView.contentMode = .scaleAspectFit
    }
}
