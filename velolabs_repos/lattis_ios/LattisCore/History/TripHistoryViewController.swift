//
//  RideHistoryViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 22/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class TripHistoryViewController: UIViewController {
    
    fileprivate let tableView = UITableView()
    fileprivate var trips: [Trip] = []
    fileprivate let dateFormatter = DateFormatter()
    fileprivate let timeFormatter = DateFormatter()
    fileprivate let durationFormatter = DateComponentsFormatter()
    fileprivate let network: TripAPI = AppRouter.shared.api()

    override func viewDidLoad() {
        Analytics.log(.history)
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }

        view.backgroundColor = .white
        title = "ride_history".localized()
        addCloseButton()
        
        view.addSubview(tableView)
        tableView.register(TripCell.self, forCellReuseIdentifier: "cell")
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 64
        tableView.sectionHeaderHeight = 44
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.tableFooterView = UIView()
        tableView.delegate = self
        tableView.dataSource = self
        
        dateFormatter.dateStyle = .medium
        dateFormatter.timeStyle = .short
        dateFormatter.doesRelativeDateFormatting = true
        
//        timeFormatter.dateStyle = .none
//        timeFormatter.timeStyle = .short
        
        durationFormatter.allowedUnits = [.day, .hour, .minute]
        durationFormatter.unitsStyle = .short
        
        constrain(tableView, view) { table, view in
            table.edges == view.edges
        }
        
        network.getTrips { [weak self] (result) in
            switch result {
            case .success(let trips):
                self?.calculate(trips: trips)
            case .failure(let error):
                self?.warning()
                Analytics.report(error)
            }
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        navigationController?.setNavigationBarHidden(false, animated: animated)
    }
    
    fileprivate func calculate(trips: [Trip]) {
        self.trips = trips.sorted(by: {$0.startedAt > $1.startedAt})
        tableView.reloadData()
    }
}

extension TripHistoryViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return trips.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! TripCell
        cell.update(trip: trips[indexPath.row], dateFormatter: dateFormatter, durationFormatter: durationFormatter)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let trip = trips[indexPath.row]
        let details = TripDetailsViewController(trip)
        navigationController?.pushViewController(details, animated: true)
    }
}
