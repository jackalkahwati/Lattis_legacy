//
//  MenuMenuViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography
import ImagePicker

class MenuViewController: ViewController, MenuInteractorOutput {
    
    var interactor: MenuInteractorInput!
    fileprivate var selectedCell: MenuCell?
    fileprivate let unselectable: [MenuItem] = [.help, .order, .terms]
    fileprivate var selectedItem: MenuItem = .home
    
    fileprivate let versionLabel = UILabel()
    fileprivate let tableView = UITableView()
    fileprivate let bottomContainer = UIView()
    fileprivate let headerView = MenuProfileView()
    fileprivate let storage: UserStorage = CoreDataStack.shared
    fileprivate var handler: StorageHandler?

    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .white
        tableView.backgroundColor = .clear
        view.addSubview(tableView)
        view.addSubview(versionLabel)
        tableView.register(MenuCell.self, forCellReuseIdentifier: "cell")
        tableView.separatorStyle = .none
        tableView.tableFooterView = UIView()
        tableView.tableHeaderView = headerView
        tableView.rowHeight = .rowHeightMedium
        tableView.alwaysBounceVertical = false
        tableView.showsVerticalScrollIndicator = false
        tableView.contentInsetAdjustmentBehavior = .never
        
        versionLabel.font = .elImageButtonTitle
        versionLabel.textColor = .elPinkishGreyTwo
        
        view.addSubview(bottomContainer)
        
        constrain(tableView, versionLabel, bottomContainer, view) { table, version, bottom, view in
            table.top == view.top
            table.left == view.left
            table.right == view.right
            
            version.bottom == view.bottom - .margin/2
            version.left == view.left + .margin
            version.right == view.right - .margin
            
            bottom.left == view.left
            bottom.right == view.right
            bottom.bottom == version.top - .margin/2
            bottom.top == table.bottom + .margin/2
        }
        
        configureBottom()

        tableView.delegate = self
        tableView.dataSource = self
        
        headerView.photoButton.addTarget(self, action: #selector(takePhoto), for: .touchUpInside)
        headerView.profileButton.addTarget(self, action: #selector(editProfile), for: .touchUpInside)
        handler = storage.current(completion: { [unowned self] (user) in
            self.headerView.user = user
        })
        
        getVersion()
    }
    
    fileprivate func configureBottom() {
        bottomContainer.backgroundColor = .clear
        let terms = UIButton(.terms)
        terms.addTarget(self, action: #selector(terms(_:)), for: .touchUpInside)
        bottomContainer.addSubview(terms)
        let logout = UIButton(.logout)
        logout.addTarget(self, action: #selector(logout(_:)), for: .touchUpInside)
        bottomContainer.addSubview(logout)
        
        constrain(terms, logout, bottomContainer) { terms, logout, container in
            terms.left == container.left
            terms.right == container.right
            terms.top == container.top
            terms.bottom == logout.top
            logout.left == terms.left
            logout.right == terms.right
            logout.bottom == container.bottom
            terms.height == tableView.rowHeight
            logout.height == terms.height
        }
    }
    
    @objc fileprivate func takePhoto() {
        func showPicker() {
            let imagePickerController = ImagePickerController()
            imagePickerController.delegate = self
            imagePickerController.imageLimit = 1
            self.present(imagePickerController, animated: true, completion: nil)
        }
        guard headerView.picture != nil else { return showPicker() }
        let alert = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        alert.addAction(UIAlertAction(title: "delete_photo".localized(), style: .destructive, handler: { (_) in
            self.headerView.picture = nil
        }))
        alert.addAction(UIAlertAction(title: "select_photo".localized(), style: .default, handler: { (_) in
            showPicker()
        }))
        alert.addAction(UIAlertAction(title: "cancel".localized().lowercased().capitalized, style: .cancel, handler: nil))
        present(alert, animated: true, completion: nil)
        
    }
    
    @objc fileprivate func editProfile() {
        interactor.didSelect(item: .profile)
    }
    
    @objc fileprivate func terms(_ selector: Any) {
        interactor.didSelect(item: .terms)
    }
    
    @objc fileprivate func logout(_ selector: Any) {
        let action = AlertView.Action(title: "log_out".localized().lowercased().capitalized) { [unowned self] _ in
            self.interactor.logOut()
        }
        AlertView.alert(title: "are_you_sure_you_want_to_log_out".localized(), actions: [.cancel, action]).show()
    }
}

fileprivate extension UIButton {
    convenience init(_ item: MenuItem) {
        self.init(type: .custom)
        setTitle(item.rawValue.localized().lowercased().capitalized, for: .normal)
        setTitleColor(.elSteel, for: .normal)
        titleLabel?.font = .elMenu
        setImage(item.icon, for: .normal)
        contentHorizontalAlignment = .left
        contentEdgeInsets = .init(top: 0, left: 20, bottom: 0, right: 20)
        titleEdgeInsets = .init(top: 0, left: 20, bottom: 0, right: 0)
    }
}

extension CGFloat {
    static let margin: CGFloat = 20
}

extension MenuViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return interactor.numberOfRows(in: section)
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! MenuCell
        let item = interactor.item(for: indexPath)
        cell.item = item
        cell.isCurrent = item == selectedItem
        if cell.isCurrent {
            selectedCell = cell
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let item = interactor.item(for: indexPath)
        interactor.didSelect(item: item)
        guard selectedItem != item, unselectable.contains(item) == false else { return }
        selectedItem = item
        selectedCell?.isCurrent = false
        selectedCell = tableView.cellForRow(at: indexPath) as? MenuCell
        selectedCell?.isCurrent = true
    }
}

private extension MenuViewController {
    func getVersion() {
        guard let version = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String,
            let build = Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String else { return }
        versionLabel.text = "\(version)(\(build))"
    }
}

extension MenuViewController: ImagePickerDelegate {
    func wrapperDidPress(_ imagePicker: ImagePickerController, images: [UIImage]) {
    }
    
    func doneButtonDidPress(_ imagePicker: ImagePickerController, images: [UIImage]) {
        imagePicker.dismiss(animated: true, completion: nil)
        guard let image = images.first else { return }
        headerView.picture = image
    }
    
    func cancelButtonDidPress(_ imagePicker: ImagePickerController) {
        dismiss(animated: true, completion: nil)
    }
}
