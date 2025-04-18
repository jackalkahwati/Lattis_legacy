//
//  AxaLockDetailsViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 18.03.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import UIKit
import Cartography

class AxaLockDetailsViewController: ViewController {
    
    fileprivate let viewModel: AxaLockDetailsViewModel
    fileprivate let tableView = UITableView()
    
    init(_ lock: AxaDevice) {
        self.viewModel = .init(lock)
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
        title = "AXA"
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "locks_action_dispatch".localized(), style: .plain, target: self, action: #selector(dispatch))
        view.backgroundColor = .white
        view.addSubview(tableView)
        
        view.addSubview(tableView)
        constrain(tableView, view) { table, view in
            table.edges == view.edges
        }
        viewModel.reloadRows = { [unowned self] paths in
            if paths.isEmpty {
                self.tableView.reloadData()
            } else {
                self.tableView.beginUpdates()
                self.tableView.reloadRows(at: paths, with: .automatic)
                self.tableView.endUpdates()
            }
        }
        viewModel.present = { [unowned self] controller in
            if let c = controller {
                self.present(c, animated: true, completion: nil)
            } else {
                self.dismiss(animated: true, completion: nil)
            }
        }
        viewModel.load = { [unowned self] flag in
            if flag {
                self.startLoading()
            } else {
                self.stopLoading()
            }
        }
        tableView.register(AxaLockDetailsCell.self, forCellReuseIdentifier: "cell")
        tableView.dataSource = self
        tableView.delegate = self
        tableView.sectionHeaderHeight = 56
        tableView.estimatedRowHeight = 44
        tableView.rowHeight = UITableView.automaticDimension
        tableView.tableFooterView = UIView()
        
        showAlert(title: "locks_connection_warning_title".localized(), subtitle: "locks_connection_warning_text".localized())
    }
    
    @objc
    fileprivate func back() {
        navigationController?.popViewController(animated: true)
    }
    
    @objc
    fileprivate func dispatch() {
        let dispatch = AxaLockDispatchViewController(viewModel.device)
        navigationController?.pushViewController(dispatch, animated: true)
    }
}

extension AxaLockDetailsViewController: UITableViewDelegate, UITableViewDataSource {
    
    func numberOfSections(in tableView: UITableView) -> Int {
        viewModel.sections.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        viewModel.numberOfRows(in: section)
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! AxaLockDetailsCell
        cell.info = viewModel.info(for: indexPath)
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
         let header = SettingsSectionView()
        header.titleLabel.text = viewModel.sections[section]
        return header
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
    }
}
