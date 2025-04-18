//
//  Annotation.swift
//  Lattis
//
//  Created by Ravil Khusainov on 21/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Mapbox

protocol AnnotationModel {
    var name: String? { get }
    var coordinate: CLLocationCoordinate2D { get }
    var image: UIImage? { get }
}

public class MapAnnotation: MGLPointAnnotation {
    var image: UIImage?
    let model: AnnotationModel
    init(model: AnnotationModel) {
        self.model = model
        super.init()
        self.coordinate = model.coordinate
        self.image = model.image
    }
    
    required public init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

class MapAnnotationView: MGLAnnotationView {
    
    private(set) var imageView: UIImageView?
    private let size: CGSize
    init(reuseIdentifier: String?, image: UIImage? = nil, size: CGSize = CGSize(width: 42, height: 90)) {
        self.size = size
        super.init(reuseIdentifier: reuseIdentifier)
        subviews.forEach{$0.removeFromSuperview()}
        frame = CGRect(x: 0, y: 0, width: size.width, height: size.height)
        let img = image ??  UIImage(named: reuseIdentifier!)
        imageView = UIImageView(image: img?.withAlignmentRectInsets(UIEdgeInsets(top: size.height/2, left: 0, bottom: 0, right: 0)))
        imageView?.translatesAutoresizingMaskIntoConstraints = false
        addSubview(imageView!)
        imageView?.constrainEdges(to: self)
        imageView?.contentMode = .scaleAspectFit
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var isChoosen: Bool = false {
        didSet {
            guard isChoosen != oldValue else { return }
            let frame: CGRect = {
                let value: CGFloat = isChoosen ? 2 : 1
                var frame = self.frame
                frame.size = size
                frame.size.width *= value
                frame.size.height *= value
                frame.origin.x -= size.width*(isChoosen ? 0.5 : -0.5)
                frame.origin.y -= size.height*(isChoosen ? 1 : -1)
                return frame
            }()
            UIView.animate(withDuration: .defaultAnimation) {
                self.centerOffset = CGVector(dx: 0, dy: -frame.height*0.5)
                self.frame = frame
            }
        }
    }
}
