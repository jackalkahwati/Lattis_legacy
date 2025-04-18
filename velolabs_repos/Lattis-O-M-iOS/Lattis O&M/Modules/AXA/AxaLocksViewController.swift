//
//  AxaLocksViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 16.03.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import UIKit
import Cartography
import AXALock

class AxaLocksViewController: ViewController {
    weak var delegate: LocksInteractorDelegate?
    fileprivate let tableView = UITableView()
    fileprivate let viewModel: AxaLocksViewModel
    fileprivate let addButton = UIButton(type: .custom)
    fileprivate let emptyView = EmptyView()
    
    init(_ filter: Lock.Filter, delegate: LocksInteractorDelegate) {
        self.delegate = delegate
        self.viewModel = .init(filter: filter)
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .white
        view.addSubview(emptyView)
        emptyView.filterButton.addTarget(self, action: #selector(openFilter), for: .touchUpInside)
        emptyView.isHidden = true
        emptyView.update(filter: self.viewModel.filter)
        tableView.register(AxaLockCell.self, forCellReuseIdentifier: "cell")
        view.addSubview(tableView)
        view.addSubview(addButton)
        
        addButton.setTitle("onboard_new_lock".localized(), for: .normal)
        if #available(iOS 13.0, *) {
            addButton.setImage(UIImage(systemName: "plus.circle"), for: .normal)
        } else {
        }
        addButton.tintColor = .white
        addButton.titleEdgeInsets = .init(top: 0, left: .margin, bottom: 0, right: 0)
        addButton.layer.cornerRadius = 5
        addButton.backgroundColor = .lsTurquoiseBlue
        addButton.addTarget(self, action: #selector(addNewLock), for: .touchUpInside)
        
        constrain(tableView, emptyView, addButton, view) { table, empty, add, view in
            table.top == view.safeAreaLayoutGuide.top
            table.left == view.left
            table.right == view.right
            
            empty.edges == table.edges
            
            add.bottom == view.safeAreaLayoutGuide.bottom - .margin/2
            add.left == view.left + .margin/2
            add.right == view.right - .margin/2
            add.height == 44
            
            table.bottom == add.top - .margin/2
        }
        viewModel.insert = { [unowned self] path in
            self.tableView.beginUpdates()
            self.tableView.insertRows(at: [path], with: .automatic)
            self.tableView.endUpdates()
        }
        viewModel.updade = { [unowned self] in
            self.tableView.reloadData()
        }
        viewModel.reload = { [unowned self] indexPath in
            self.tableView.beginUpdates()
            self.tableView.reloadRows(at: [indexPath], with: .automatic)
            self.tableView.endUpdates()
        }
        viewModel.failure = { [unowned self] error in
            self.show(error: error)
        }
        viewModel.empty = { [unowned self] isEmpty in
            self.tableView.isHidden = isEmpty
            self.emptyView.isHidden = !isEmpty
        }
        tableView.tableFooterView = UIView()
        tableView.estimatedRowHeight = 44
        tableView.rowHeight = UITableView.automaticDimension
        tableView.dataSource = self
        tableView.delegate = self
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        viewModel.fetchModules()
    }
    
    @objc
    fileprivate func addNewLock() {
        let onboarding = AxaLockQRScannerViewController { [unowned self] device in
            self.dismiss(animated: true, completion: nil)
            self.viewModel.onboard(device: device)
            self.openDetails(device: device)
        }
        let navigation = UINavigationController(rootViewController: onboarding, style: .blue)
        present(navigation, animated: true, completion: nil)
    }
    
    fileprivate func openDetails(device: AxaDevice) {
        let details = AxaLockDetailsViewController(device)
        viewModel.sendOutOfService(device: device)
        navigationController?.pushViewController(details, animated: true)
    }
}

extension AxaLocksViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        viewModel.devices.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! AxaLockCell
        cell.device = viewModel.devices[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 44
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let header = LocksFilterSectionView()
        header.titleLabel.text = "axa_locks".localized()
        header.subtitleLabel.text = "Filter: \(viewModel.filter.title)"
        header.delegate = self
        return header
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let device = viewModel.devices[indexPath.row]
        let alert = AlertController(title: device.bike?.name ?? device.name, message: "confirm_action".localized())
        if device.isPaired {
            alert.actions = [
                .plain(title: "locks_action_settings".localized()) { [unowned self] in self.openDetails(device: device)},
                .plain(title: "locks_action_disconnect".localized(), handler: device.lock.disconnect)
            ]
        } else {
            alert.actions = [
                .plain(title: "locks_action_connect".localized()) { [unowned self] in
                    self.startLoading()
                    self.viewModel.connect(lockAt: indexPath) { [unowned self] device in
                        self.stopLoading()
                        self.openDetails(device: device)
                    }
                }
            ]
        }
        alert.actions.append(.cancel)
        present(alert, animated: true, completion: nil)
        tableView.deselectRow(at: indexPath, animated: true)
    }
}

extension AxaLocksViewController: LocksFilterSectionDelegate {
    func openSearch() {
        
    }
    
    @objc
    func openFilter() {
        let filterVC = LocksFilterViewController(delegate: self, filter: viewModel.filter, vendor: .axa)
        let navigation = UINavigationController(rootViewController: filterVC, style: .blue)
        present(navigation, animated: true, completion: nil)
    }
    
    func didSelect(filter: Lock.Filter, vendor: Lock.Vendor) {
        viewModel.change(filter: filter)
        delegate?.change(vendor: vendor, filter: filter)
        emptyView.update(filter: filter)
    }
}

fileprivate extension AxaLocksViewController {
    final class EmptyView: UIView {
        fileprivate let warningLabel = UILabel()
        fileprivate let filterLabel = UILabel()
        fileprivate let filterButton = UIButton(type: .custom)
        
        override init(frame: CGRect) {
            super.init(frame: frame)
            
            warningLabel.text = "There are no AXA locks around"
            warningLabel.textAlignment = .center
            warningLabel.numberOfLines = 0
            
            filterLabel.text = "Filter:"
            filterLabel.textAlignment = .center
            
            filterButton.contentEdgeInsets = .init(top: .margin, left: .margin, bottom: .margin, right: .margin)
            filterButton.layer.cornerRadius = 5
            filterButton.backgroundColor = .lsTurquoiseBlue
            filterButton.setTitleColor(.white, for: .normal)
            
            let contentView = UIStackView(arrangedSubviews: [warningLabel, filterLabel, filterButton])
            contentView.axis = .vertical
            contentView.spacing = .margin
            addSubview(contentView)
            
            constrain(contentView, self) { content, view in
                content.left == view.left + .margin
                content.right == view.right - .margin
                content.centerY == view.centerY
            }
        }
        
        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
        
        func update(filter: Lock.Filter) {
            filterButton.setTitle(filter.title, for: .normal)
        }
    }
}
