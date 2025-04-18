//
//  RideHistoryListRideHistoryListViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 18/08/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

class RideHistoryListViewController: ViewController {
    var interactor: RideHistoryListInteractorInput!
    
    @IBOutlet weak var tableView: UITableView!
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        navigationItem.leftBarButtonItem = .menu(target: self, action: #selector(menu))
        title = "menu_history".localized().uppercased()
        navigationController?.navigationBar.set(style: .blue)
        navigationController?.setNavigationBarHidden(false, animated: true)
        
        view.backgroundColor = .white
        
        tableView.register(UINib(nibName: "RideHistoryListCell", bundle: nil), forCellReuseIdentifier: "cell")
        tableView.contentInset = UIEdgeInsets(top: tableView.contentInset.top + 16, left: 0, bottom: 0, right: 0)
        tableView.tableFooterView = UIView()
        tableView.delegate = self
        tableView.dataSource = self
        interactor.viewModel.refresh = { [unowned self] hidden in
            self.tableView.isHidden = hidden
            self.tableView.reloadData()
        }
        interactor.viewModel.start()
        
    }
    
    @IBAction func findBike(_ sender: Any) {
        AppRouter.shared.searchBike()
    }
}

extension RideHistoryListViewController: RideHistoryListInteractorOutput {
	
}

extension RideHistoryListViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return interactor.viewModel.tripsCount
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! RideHistoryListCell
        cell.trip = interactor.viewModel.trip(for: indexPath)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        interactor.didSelectRow(at: indexPath)
    }
}
