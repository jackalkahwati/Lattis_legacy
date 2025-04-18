//
//  BikeDetailsViewController_v2.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 24/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Kingfisher
import SafariServices
import Model

public class BikeDetailsViewController_v2: UIViewController {

    let closeButton = UIButton(type: .custom)
    fileprivate let tableView = UITableView()
    fileprivate let bikeImageView = UIImageView(image: .named("kickscooter_preview"))
    fileprivate let topView = UIView()
    
    fileprivate let bike: Model.Bike
    fileprivate var sections: [Section] = []
    
    public init(_ bike: Model.Bike) {
        self.bike = bike
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .white
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        topView.backgroundColor = .white
        view.addSubview(topView)
        
        bikeImageView.kf.setImage(with: bike.bikeGroup.pic, completionHandler:  { [weak self] (result) in
            self?.renderHeader()
        })
        bikeImageView.contentMode = .scaleAspectFit
        
        view.addSubview(tableView)
        tableView.backgroundColor = .clear
        tableView.tableFooterView = UIView()
        tableView.estimatedRowHeight = 44
        tableView.separatorStyle = .none
        tableView.rowHeight = UITableView.automaticDimension
        
        closeButton.setImage(.named("icon_close"), for: .normal)
        topView.addSubview(closeButton)
        
        constrain(topView, tableView, closeButton, view) { top, table, close, view in
            top.top == view.top
            top.left == view.left
            top.right == view.right
            top.height == 44
            
            table.bottom == view.bottom
            table.left == view.left
            table.right == view.right
            table.top == top.bottom
            
            close.right == top.right - .margin
            close.top == top.top
            close.bottom == top.bottom
        }
        
        var priceSection: [BikeInfo] = []
        if let price = bike.fleet.paymentSettings?.price {
            priceSection.append(.tuple("bike_detail_label_price".localized(),  price))
        }
        if let unlock = bike.fleet.paymentSettings?.unlockPrice {
            priceSection.append(.tuple("unlock_fee".localized(), unlock))
        }
        if let surcharge = bike.fleet.paymentSettings?.surchargePrice {
            priceSection.append(.tuple("surcharge".localized(), surcharge))
            
            if let desc = bike.fleet.paymentSettings?.surchargeDescription {
                priceSection.append(.info(desc))
            }
        }
        if bike.fleet.isFree == false, let parking = bike.fleet.paymentSettings?.parkingPrice {
            priceSection += [
                .tuple("bike_detail_label_parking_fee".localized(), parking),
                .info("bike_detail_label_parking_fee_warning".localized())
            ]
        }
        
        var network: [BikeInfo] = [.fleet(bike.fleet.name)]

        network.append(.termsOfUse)
        network.append(.parkingSpots)
        
        sections = [
            .init(title: nil, items: [.bike_v2(bike)]),
            .init(title: "bike_detail_label_trip_costs".localized(), items: priceSection),
            .init(title: "bike_detail_label_about_the_bike".localized(), items: [
                .tuple("bike_detail_label_name".localized(), bike.bikeName),
                .tuple("bike_detail_label_model".localized(), bike.bikeGroup.type.localizedTitle),
                .details(bike.bikeGroup.description)
                ]),
            .init(title: "bike_detail_label_network".localized(), items: network)
        ]
        tableView.register(BikeInfoCell.self, forCellReuseIdentifier: "info")
        tableView.register(BikeInfoTupleCell.self, forCellReuseIdentifier: "tuple")
        tableView.register(BikeInfoBikeCell_v2.self, forCellReuseIdentifier: "bike")
        tableView.register(BikeDetailsCell.self, forCellReuseIdentifier: "details")
        tableView.register(BikeInfoDisclosureCell.self, forCellReuseIdentifier: "disclosure")
        tableView.register(BikeInfoSectionView.self, forHeaderFooterViewReuseIdentifier: "header")
        tableView.dataSource = self
        tableView.delegate = self
    }
    
    fileprivate func openFleetDetails() {
        //TODO:
    }
    
    public override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        renderHeader()
    }
    
    fileprivate func renderHeader() {
        guard let image = bikeImageView.image, tableView.tableHeaderView == nil, tableView.bounds.width > 0 else { return }
        bikeImageView.frame = {
            var frame = tableView.bounds
            frame.size.height = image.size.height * (frame.width/image.size.width)
            return frame
        }()
        tableView.tableHeaderView =  bikeImageView
    }
    
    fileprivate func openParkingZones() {
        let zones = ParkingZonesViewController(bike.fleet.fleetId)
        let map = AppRouter.shared.map(zones)
        present(map, animated: true, completion: nil)
    }
    
    fileprivate func openTermsAndConditions() {
        guard let legal = bike.fleet.legal,
            let url = URL(string: legal) else { return warning() }
        let safari = SFSafariViewController(url: url)
        present(safari, animated: true, completion: nil)
    }
}

extension BikeDetailsViewController_v2: UITableViewDataSource, UITableViewDelegate {
    
    public func numberOfSections(in tableView: UITableView) -> Int {
        return sections.count
    }
    
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return sections[section].items.count
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let info = sections[indexPath.section].items[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: info.identifier, for: indexPath) as! BikeInfoRepresenting
        cell.info = info
        return cell
    }
    
    public func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        guard let title = sections[section].title else { return nil }
        let view = tableView.dequeueReusableHeaderFooterView(withIdentifier: "header") as? BikeInfoSectionView
        view?.title = title
        return view
    }
    
    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let info = sections[indexPath.section].items[indexPath.row]
        switch info {
        case .fleet:
            openFleetDetails()
        case .termsOfUse:
            openTermsAndConditions()
        case .parkingSpots:
            openParkingZones()
        default:
            break
        }
    }
    
    public func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        guard section < sections.count - 1 else { return nil }
        let view = UIView()
        view.backgroundColor = .white
        let line = UIView.line
        view.addSubview(line)
        constrain(line, view) {
            $0.centerY == $1.centerY
            $0.left == $1.left + .margin
            $0.right == $1.right - .margin
        }
        return view
    }
    
    public func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        guard section < sections.count - 1 else { return 0 }
        return .margin
    }
    
    public func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        guard sections[section].title != nil else { return 0 }
        return 50
    }
    
    struct Section {
        let title: String?
        let items: [BikeInfo]
    }
}
