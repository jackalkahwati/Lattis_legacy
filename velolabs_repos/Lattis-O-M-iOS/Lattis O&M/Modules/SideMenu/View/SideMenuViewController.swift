//
//  SideMenuSideMenuViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 05/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LGSideMenuController


class SideMenuViewController: ViewController {
    @IBOutlet weak var tableView: UITableView!
    var interactor: SideMenuInteractorInput!

    fileprivate var fleets: [Fleet] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()

        tableView.register(FleetCell.self, forCellReuseIdentifier: "FleetCell")
        tableView.tableFooterView = UIView()
        tableView.dataSource = self
        tableView.delegate = self
        
        interactor.viewLoaded()
    }
    
    @IBAction func about(_ sender: Any) {
        
    }
    
    @IBAction func logOut(_ sender: Any) {
        AppDelegate.shared.logout()
    }
}

extension SideMenuViewController: SideMenuInteractorOutput {
    func show(fleets: [Fleet]) {
        self.fleets = fleets
        tableView.reloadData()
        sideMenuController?.hideLeftViewAnimated(sender: self)
    }
}

extension SideMenuViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return fleets.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "FleetCell", for: indexPath) as! FleetCell
        cell.fleet = fleets[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let fleet = fleets[indexPath.row]
        guard  let isCurrent = fleet.isCurrent, isCurrent == false else {
            sideMenuController?.hideLeftViewAnimated(sender: self)
            return
        }
        interactor.select(fleet: fleet)
    }
}
