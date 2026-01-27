package io.mvnpm.maven.locker;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.util.Predicate;

public final class XmlUnitTestSupport {
    private XmlUnitTestSupport() {
    }

    public static Predicate<Node> ignoreMvnpmDependencyVersions() {
        return node -> {
            if (!(node instanceof Element) || !"version".equals(node.getNodeName())) {
                return true;
            }
            Node parent = node.getParentNode();
            if (!(parent instanceof Element) || !"dependency".equals(parent.getNodeName())) {
                return true;
            }
            Node groupIdNode = ((Element) parent).getElementsByTagName("groupId").item(0);
            String groupId = groupIdNode == null ? null : groupIdNode.getTextContent();
            return groupId == null || (!groupId.trim().startsWith("org.mvnpm") && !groupId.trim().startsWith("org.webjars"));
        };
    }
}
