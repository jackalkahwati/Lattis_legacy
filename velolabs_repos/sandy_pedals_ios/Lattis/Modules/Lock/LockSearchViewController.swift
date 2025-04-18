//
//  LockSearchViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 17/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class LockSearchViewController: ViewController {
    @IBOutlet var searchView: LockSearchView!
    var locks: [Lock] = []
    static var navigation: UINavigationController {
        return UINavigationController(rootViewController: LockSearchViewController(), style: .grey)
    }
    
    init() {
        super.init(nibName: "LockSearchView", bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "SEARCH LOCKS".localized()
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close(_:)))
        navigationController?.isNavigationBarHidden = false
        searchView.tableView.register(EllipseRow.self, forCellReuseIdentifier: "EllipseRow")
        searchView.tableView.dataSource = self
        searchView.tableView.delegate = self
        LocksService.shared.startScan()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        LocksService.shared.subscribe(request: LocksService.Request(target: self), completion: update(locks: ))
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        LocksService.shared.unsubscribe(target: self)
    }
    
    func update(locks: [Lock]) {
        self.locks = locks
        DispatchQueue.main.async {
            self.searchView.tableView.reloadData()
        }
    }
    
    @objc private func close(_ sender: Any) {
        navigationController?.dismiss(animated: true, completion: nil)
    }
}

extension LockSearchViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return locks.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "EllipseRow", for: indexPath) as! EllipseRow
        cell.lock = locks[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
//        let controller = LockSettingsViewController()
//        controller.lock = locks[indexPath.row]
//        navigationController?.pushViewController(controller, animated: true)
    }
}

class EllipseRow: UITableViewCell {
    var lock: Lock? {
        didSet {
            textLabel?.text = lock?.peripheral?.name
        }
    }
}
