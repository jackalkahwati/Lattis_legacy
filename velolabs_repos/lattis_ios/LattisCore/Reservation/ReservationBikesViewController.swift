//
//  ReservationBikesViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 10.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

class ReservationBikesViewController: UIViewController {
    
    fileprivate let bikes: [Model.Bike]
    fileprivate var selected: Model.Bike?
    fileprivate var selectedIndexPath: IndexPath?
    fileprivate let onSelect: (Model.Bike) -> ()
    
    fileprivate let tableView = UITableView()
    
    init(bikes: [Model.Bike], selected: Model.Bike?, onSelect: @escaping (Model.Bike) -> ()) {
        self.bikes = bikes
        self.selected = selected
        self.onSelect = onSelect
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        title = "select_vehicle".localized()
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        view.backgroundColor = .white
        addCloseButton()
        
        view.addSubview(tableView)
        
        constrain(tableView, view) { $0.edges == $1.edges }
        
        tableView.register(ReservationBikeCell.self, forCellReuseIdentifier: "cell")
        tableView.estimatedRowHeight = 100
        tableView.rowHeight = UITableView.automaticDimension
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.tableFooterView = UIView()
        tableView.dataSource = self
        tableView.delegate = self
    }
    
    fileprivate func selectBike(at indesPath: IndexPath) {
        var toUpdate: [IndexPath] = [indesPath]
        selected = bikes[indesPath.row]
        if let sel = selectedIndexPath {
            if sel != indesPath {
                toUpdate.append(sel)
            } else {
                selected = nil
                selectedIndexPath = nil
            }
        }
        tableView.beginUpdates()
        tableView.reloadRows(at: toUpdate, with: .automatic)
        tableView.endUpdates()
        if let bike = selected {
            onSelect(bike)
        }
    }
    
    fileprivate func openInfo(_ bike: Model.Bike) {
        let details = BikeDetailsViewController_v2(bike)
        details.closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)
        present(details, animated: true)
    }
    
    fileprivate func showOnMap(_ bike: Model.Bike) {
        let map = BikeLocationViewController.map(bike)
        present(map, animated: true)
    }
}

extension ReservationBikesViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        bikes.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! ReservationBikeCell
        let bike = bikes[indexPath.row]
        let isSelected = selected == bike
        if isSelected { selectedIndexPath = indexPath }
        cell.update(bike: bike, isSelected: isSelected, info: { [unowned self] in
            self.openInfo(bike)
        },
        locate: { [unowned self] in
            self.showOnMap(bike)
        }) { [unowned self] in
            self.selectBike(at: indexPath)
        }
        return cell
    }
}

final class ReservationBikeCell: UITableViewCell {
    fileprivate var bikeControl: BikeItem?
    fileprivate let bikeContainer = UIView()
    fileprivate let controlsContainer = UIStackView()
    fileprivate let selectButton = ActionButton()
    fileprivate let infoButton = UIButton(type: .custom)
    fileprivate let mapButton = UIButton(type: .custom)
    fileprivate var info: () -> Void = {}
    fileprivate var locate: () -> Void = {}

    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = .none
        
        contentView.addSubview(bikeContainer)
        contentView.addSubview(controlsContainer)
        controlsContainer.axis = .horizontal
        controlsContainer.spacing = .margin
        
        constrain(bikeContainer, controlsContainer, contentView) { bike, controls, content in
            controls.bottom == content.bottom - .margin
            controls.left == content.left + .margin
            controls.right == content.right - .margin
            
            bike.top == content.top + .margin
            bike.left == content.left + .margin
            bike.right == content.right - .margin
            bike.bottom == controls.top - .margin/2
        }
        
        infoButton.setImage(.named("icon_info"), for: .normal)
        infoButton.tintColor = .black
        infoButton.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        infoButton.addTarget(self, action: #selector(openBikeInfo), for: .touchUpInside)
        
        mapButton.setImage(.named("icon_map_bike"), for: .normal)
        mapButton.tintColor = .black
        mapButton.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        mapButton.addTarget(self, action: #selector(openMap), for: .touchUpInside)
        
        controlsContainer.addArrangedSubview(mapButton)
        controlsContainer.addArrangedSubview(infoButton)
        controlsContainer.addArrangedSubview(selectButton)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func update(bike: Model.Bike, isSelected: Bool, info: @escaping () -> Void, locate: @escaping () -> Void, onSelect: @escaping () -> Void) {
        bikeControl?.removeFromSuperview()
        bikeControl = .init(bike: bike)
        bikeControl?.isUserInteractionEnabled = false
        bikeContainer.addSubview(bikeControl!)
        constrain(bikeControl!, bikeContainer) { $0.edges == $1.edges }
        layoutIfNeeded()
        
        self.info = info
        self.locate = locate
        selectButton.action = .plain(title: isSelected ? "selected".localized() : "select".localized(), style: isSelected ? .active : .inactiveSecondary, handler: onSelect)
    }
    
    @objc
    fileprivate func openBikeInfo() {
        info()
    }
    
    @objc
    fileprivate func openMap() {
        locate()
    }
}
