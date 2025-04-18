//
//  ReservationFleetsViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 18.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

class ReservationFleetsViewController: UIViewController {
    
    fileprivate var fleets: [Model.Fleet] = []
    fileprivate let network: FleetsNetwork = AppRouter.shared.api()
    
    fileprivate let tableView = UITableView()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "select_fleet".localized()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        view.backgroundColor = .white

        view.addSubview(tableView)
        
        tableView.register(ReservationFleetCell.self, forCellReuseIdentifier: "fleet")
        tableView.dataSource = self
        tableView.delegate = self
        tableView.estimatedRowHeight = 88
        tableView.rowHeight = UITableView.automaticDimension
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.tableFooterView = UIView()
        
        constrain(tableView, view) { $0.edges == $1.edges }
        fetchData()
    }
    
    fileprivate func fetchData() {
        network.fetchFleets { [weak self] (result) in
            switch result {
            case .success(let fleets):
                self?.fleets = fleets.filter(\.isReservationEnabled)
                self?.tableView.reloadData()
            case .failure(let error):
                self?.handle(error)
            }
        }
    }
}

extension ReservationFleetsViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        fleets.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "fleet", for: indexPath) as! ReservationFleetCell
        cell.fleet = fleets[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let fleet = fleets[indexPath.row]
        guard let settings = fleet.reservationSettings else { return }
        let reservation = NewReservationViewController(fleet: fleet, settings: settings)
        navigationController?.pushViewController(reservation, animated: true)
    }
}
